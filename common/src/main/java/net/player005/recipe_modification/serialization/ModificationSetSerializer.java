package net.player005.recipe_modification.serialization;

import com.google.gson.JsonElement;
import net.player005.recipe_modification.api.ModificationSet;
import net.player005.recipe_modification.api.RecipeModifier;

public abstract class ModificationSetSerializer {
    public static ModificationSet fromJson(JsonElement json) {
        if (json.isJsonArray()) {
            var modifiers = new RecipeModifier[json.getAsJsonArray().size()];
            for (int i = 0; i < modifiers.length; i++) {
                modifiers[i] = RecipeModifierSerializer.fromJson(json.getAsJsonArray().get(i));
            }
            return new ModificationSet(modifiers);
        } else if (json.isJsonObject())
            return new ModificationSet(RecipeModifierSerializer.fromJson(json));

        else throw new RecipeModifierParsingException("Invalid recipe modifier list: expected either" +
                    " an array of modifiers or a single modifier object.");
    }
}
