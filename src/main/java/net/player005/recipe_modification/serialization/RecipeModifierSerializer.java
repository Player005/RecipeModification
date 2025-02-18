package net.player005.recipe_modification.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
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
            var ingredient = Ingredient.fromJson(object.get("ingredient"));
            return RecipeModifier.addIngredient(ingredient);
        });

        registerDeserializer("remove_ingredient", object -> {
            var ingredientSelector = IngredientSelectorSerializer.fromJson(object.get("ingredients"));
            return RecipeModifier.removeIngredients(ingredientSelector);
        });

        registerDeserializer("replace_ingredient", object -> {
            var ingredientSelector = IngredientSelectorSerializer.fromJson(object.get("ingredient"));
            var newIngredient = Ingredient.fromJson(object.get("new_ingredient"));
            return RecipeModifier.replaceIngredient(ingredientSelector, newIngredient);
        });

        registerDeserializer("add_alternative", object -> {
            var ingredientSelector = IngredientSelectorSerializer.fromJson(object.get("ingredients"));
            var alternative = Ingredient.fromJson(object.get("alternative"));
            return RecipeModifier.addAlternative(ingredientSelector, alternative);
        });

        registerDeserializer("replace_result", object -> {
            var newResult = ItemStack.CODEC.parse(JsonOps.INSTANCE, object.get("new_result"))
                    .getOrThrow(true, err -> {
                        throw new RecipeModifierParsingException("Invalid new result: " + err);
                    });
            return RecipeModifier.replaceResultItem(newResult);
        });

//        registerDeserializer("modify_result", object -> { TODO: result modifiers
//            var function = LootItemFunctionType
//        });
    }

    public static void registerDeserializer(String id, Function<JsonObject, RecipeModifier> deserializer) {
        deserializers.put(id, deserializer);
    }
}
