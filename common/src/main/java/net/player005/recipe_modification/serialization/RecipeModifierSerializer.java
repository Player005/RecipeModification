package net.player005.recipe_modification.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.player005.recipe_modification.api.RecipeModification;
import net.player005.recipe_modification.api.RecipeModifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class RecipeModifierSerializer {
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
            return RecipeModifier.addIngredient(ingredient);
        });

        registerDeserializer("remove_ingredients", object -> {
            var ingredientSelector = IngredientSelectorSerializer.fromJson(object.get("ingredients"));
            return RecipeModifier.removeIngredients(ingredientSelector);
        });

        registerDeserializer("replace_ingredients", object -> {
            var ingredientSelector = IngredientSelectorSerializer.fromJson(object.get("ingredient"));
            var newIngredient = Ingredient.CODEC.parse(JsonOps.INSTANCE, object.get("new_ingredient")).getOrThrow();
            return RecipeModifier.replaceIngredient(ingredientSelector, newIngredient);
        });

        registerDeserializer("add_alternative", object -> {
            var ingredientSelector = IngredientSelectorSerializer.fromJson(object.get("ingredients"));
            var alternative = Ingredient.CODEC.parse(JsonOps.INSTANCE, object.get("alternative")).getOrThrow();
            return RecipeModifier.addAlternative(ingredientSelector, alternative);
        });

        registerDeserializer("replace_result_item", object -> {
            var newResult = ItemStack.CODEC.parse(JsonOps.INSTANCE, object.get("new_result")).getOrThrow();
            return RecipeModifier.replaceResultItem(newResult);
        });

        registerDeserializer("modify_result_item", object -> {
            var function = LootItemFunctions.CODEC.parse(JsonOps.INSTANCE, object).getOrThrow().value();
            return RecipeModifier.modifyResultItem(itemStack -> function.apply(itemStack, null));
        });

        registerDeserializer("remove_recipe", object -> (recipe, helper) ->
            RecipeModification.removeRecipe(RecipeModification.findRecipeID(recipe)));
    }

    public static void registerDeserializer(String id, Function<JsonObject, RecipeModifier> deserializer) {
        deserializers.put(id, deserializer);
    }
}
