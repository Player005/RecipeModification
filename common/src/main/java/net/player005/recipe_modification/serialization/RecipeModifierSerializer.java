package net.player005.recipe_modification.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
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
        var serializationContext = RecipeModification.getRegistryAccess().createSerializationContext(JsonOps.INSTANCE);

        registerDeserializer("add_ingredient", object -> {
            var ingredient = Ingredient.CODEC.parse(serializationContext, object.get("ingredient")).getOrThrow();
            return RecipeModifier.addIngredient(ingredient);
        });

        registerDeserializer("remove_ingredient", object -> {
            var ingredientSelector = IngredientSelectorSerializer.fromJson(object.get("ingredients"));
            return RecipeModifier.removeIngredients(ingredientSelector);
        });

        registerDeserializer("replace_ingredient", object -> {
            var ingredientSelector = IngredientSelectorSerializer.fromJson(object.get("ingredient"));
            var newIngredient = Ingredient.CODEC.parse(serializationContext, object.get("new_ingredient")).getOrThrow();
            return RecipeModifier.replaceIngredient(ingredientSelector, newIngredient);
        });

        registerDeserializer("add_alternative", object -> {
            var ingredientSelector = IngredientSelectorSerializer.fromJson(object.get("ingredients"));
            var alternative = Ingredient.CODEC.parse(serializationContext, object.get("alternative")).getOrThrow();
            return RecipeModifier.addAlternative(ingredientSelector, alternative);
        });

        registerDeserializer("replace_result", object -> {
            var newResult = ItemStack.CODEC.parse(serializationContext, object.get("new_result")).getOrThrow();
            return RecipeModifier.replaceResultItem(newResult);
        });

        registerDeserializer("modify_result_components", object -> {
            var patch = DataComponentPatch.CODEC.parse(serializationContext, object.get("components")).getOrThrow();
            return RecipeModifier.addResultComponents(patch);
        });
    }

    public static void registerDeserializer(String id, Function<JsonObject, RecipeModifier> deserializer) {
        deserializers.put(id, deserializer);
    }
}
