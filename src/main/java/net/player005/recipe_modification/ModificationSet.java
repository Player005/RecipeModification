package net.player005.recipe_modification;

import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

public record ModificationSet(RecipeModifier[] modifiers) implements Iterable<RecipeModifier> {

    public void apply(Recipe<?> recipe, ModificationHelper helper) {
        for (var modifier : this) {
            modifier.apply(recipe, helper);
        }
    }

    @Override
    public @NotNull Iterator<RecipeModifier> iterator() {
        return Arrays.stream(modifiers).iterator();
    }
}
