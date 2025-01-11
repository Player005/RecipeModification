package net.player005.recipe_modification;

import com.google.gson.JsonElement;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

public record ModificationSet(RecipeModifier... modifiers) implements Iterable<RecipeModifier> {

    public void apply(Recipe<?> recipe, ModificationHelper helper) {
        for (var modifier : this) {
            modifier.apply(recipe, helper);
        }
    }

    @Override
    public @NotNull Iterator<RecipeModifier> iterator() {
        return Arrays.stream(modifiers).iterator();
    }

    public static abstract class Serialization {
        public static ModificationSet fromJson(JsonElement json) {
            if (json.isJsonArray()) {
                var modifiers = new RecipeModifier[json.getAsJsonArray().size()];
                for (int i = 0; i < modifiers.length; i++) {
                    modifiers[i] = RecipeModifier.Serialization.fromJson(json.getAsJsonArray().get(i));
                }
                return new ModificationSet(modifiers);
            }
            else if (json.isJsonObject())
                return new ModificationSet(RecipeModifier.Serialization.fromJson(json));

            else throw new RecipeModifierParsingException("Invalid recipe modifier list: expected either" +
                        " an array of modifiers or a single modifier object.");
        }
    }
}
