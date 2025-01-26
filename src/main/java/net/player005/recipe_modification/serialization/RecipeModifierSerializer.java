package net.player005.recipe_modification.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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
            var ingredient = Ingredient.CODEC.parse(JsonOps.INSTANCE, object.get("ingredient")).getPartialOrThrow();
            return RecipeModifier.addIngredient(ingredient);
        });

        registerDeserializer("remove_ingredient", object -> {
            var ingredientSelector = IngredientSelectorSerializer.fromJson(object.get("ingredients"));
            return RecipeModifier.removeIngredients(ingredientSelector);
        });

        registerDeserializer("replace_ingredient", object -> {
            var ingredientSelector = IngredientSelectorSerializer.fromJson(object.get("ingredient"));
            var newIngredient = Ingredient.CODEC.parse(JsonOps.INSTANCE, object.get("new_ingredient")).getPartialOrThrow();
            return RecipeModifier.replaceIngredient(ingredientSelector, newIngredient);
        });

        registerDeserializer("add_alternative", object -> {
            var ingredientSelector = IngredientSelectorSerializer.fromJson(object.get("ingredients"));
            var alternative = Ingredient.CODEC.parse(JsonOps.INSTANCE, object.get("alternative")).getPartialOrThrow();
            return RecipeModifier.addAlternative(ingredientSelector, alternative);
        });

        registerDeserializer("replace_result", object -> {
            var newResult = ItemStack.CODEC.parse(JsonOps.INSTANCE, object.get("new_result")).getPartialOrThrow();
            return RecipeModifier.replaceResultItem(newResult);
        });

        registerDeserializer("modify_result_components", object -> {
            var patch = DataComponentPatch.CODEC.parse(JsonOps.INSTANCE, object.get("components")).getPartialOrThrow();
            return RecipeModifier.addResultComponents(patch);
        });
    }

    public static void registerDeserializer(String id, Function<JsonObject, RecipeModifier> deserializer) {
        deserializers.put(id, deserializer);
    }
}
