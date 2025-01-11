package net.player005.recipe_modification;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
public interface RecipeModifier {

    void apply(Recipe<?> recipe, ModificationHelper helper);

    /**
     * Removes all ingredients that match the given item.
     */
    static RecipeModifier removeAllIngredients(Item item) {
        return removeIngredients(IngredientSelector.matchingItem(item));
    }

    /**
     * Tries to remove all ingredients that match the given selector.
     */
    static RecipeModifier removeIngredients(IngredientSelector selector) {
        return (recipe, helper) -> {
            for (var ingredient : selector.selectIngredients(recipe)) {
                helper.removeIngredient(ingredient);
            }
        };
    }

    /**
     * Tries to add the given ingredient to the recipe.
     */
    static RecipeModifier addIngredient(Ingredient ingredient) {
        return (recipe, helper) -> helper.addIngredient(ingredient);
    }

    /**
     * Removes the ingredient at the given ordinal.
     */
    static RecipeModifier removeIngredient(int ordinal) {
        return (recipe, helper) -> helper.removeIngredient(recipe.getIngredients().get(ordinal));
    }

    /**
     * Add the given ingredient value as an alternative to matching ingredients.
     */
    static RecipeModifier addAlternative(IngredientSelector selector, Ingredient.Value alternative) {
        return (recipe, helper) -> helper.addAlternative(selector, alternative);
    }

    /**
     * Add the given item as an alternative to matching ingredients.
     */
    static RecipeModifier addAlternative(Item original, Item alternative) {
        return (recipe, helper) -> helper.addAlternative(IngredientSelector.matchingItem(original),
                new Ingredient.ItemValue(alternative.getDefaultInstance()));
    }

    /**
     * Add the given tag as an alternative to matching ingredients.
     */
    static RecipeModifier addAlternative(Item original, TagKey<Item> alternative) {
        return (recipe, helper) -> helper.addAlternative(IngredientSelector.matchingItem(original),
                new Ingredient.TagValue(alternative));
    }

    /**
     * Replaces all ingredients that match the given selector with the new ingredient
     */
    static RecipeModifier replaceIngredient(IngredientSelector selector, Ingredient newIngredient) {
        return (recipe, helper) -> {
            for (var ingredient : selector.selectIngredients(recipe)) {
                helper.replaceIngredient(ingredient, newIngredient);
            }
        };
    }

    static RecipeModifier modifyResultItem(Function<ItemStack, ItemStack> modifier) {
        return (recipe, helper) -> RecipeModification.registerRecipeResultModifier(recipe, (recipe1, result, recipeInput) -> modifier.apply(result));
    }

    static RecipeModifier replaceResultItem(ItemStack newResult) {
        return modifyResultItem(stack -> newResult);
    }

    static RecipeModifier addResultComponents(DataComponentPatch patch) {
        return modifyResultItem(stack -> {
            stack.applyComponents(patch);
            return stack;
        });
    }

    abstract class Serialization {
        private static final Map<String, Function<JsonObject, RecipeModifier>> deserializers = new HashMap<>();

        public static RecipeModifier fromJson(JsonElement json) {
            var object = json.getAsJsonObject();
            var modifierId = object.get("type").getAsString();

            if (!deserializers.containsKey(modifierId)) {
                throw new RecipeModifierParsingException("Unknown recipe modifier type: " + modifierId);
            }

            return deserializers.get(modifierId).apply(object);
        }

        static {
            registerDeserializer("add_ingredient", object -> {
                var ingredient = Ingredient.CODEC.parse(JsonOps.INSTANCE, object.get("ingredient")).getOrThrow();
                return addIngredient(ingredient);
            });

            registerDeserializer("remove_ingredient", object -> {
                var ingredientSelector = IngredientSelector.Serialization.fromJson(object.get("ingredients"));
                return removeIngredients(ingredientSelector);
            });

            registerDeserializer("replace_ingredient", object -> {
                var ingredientSelector = IngredientSelector.Serialization.fromJson(object.get("ingredients"));
                var newIngredient = Ingredient.CODEC.parse(JsonOps.INSTANCE, object.get("new_ingredient")).getOrThrow();
                return replaceIngredient(ingredientSelector, newIngredient);
            });

            registerDeserializer("add_alternative", object -> {
                var ingredientSelector = IngredientSelector.Serialization.fromJson(object.get("ingredients"));
                var alternative = Ingredient.Value.CODEC.parse(JsonOps.INSTANCE, object.get("alternative")).getOrThrow();
                return addAlternative(ingredientSelector, alternative);
            });

            registerDeserializer("replace_result", object -> {
                var newResult = ItemStack.CODEC.parse(JsonOps.INSTANCE, object.get("new_result")).getOrThrow();
                return replaceResultItem(newResult);
            });

            registerDeserializer("modify_result_components", object -> {
                var patch = DataComponentPatch.CODEC.parse(JsonOps.INSTANCE, object.get("components")).getOrThrow();
                return addResultComponents(patch);
            });
        }

        public static void registerDeserializer(String id, Function<JsonObject, RecipeModifier> deserializer) {
            deserializers.put(id, deserializer);
        }
    }
}
