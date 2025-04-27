package net.player005.recipe_modification.impl;

import net.minecraft.world.item.crafting.Ingredient;

public interface IngredientAccessor {

    default void replaceValues(Ingredient.Value[] values) {
        throw new RuntimeException();
    }

    default Ingredient.Value[] getValues() {
        throw new RuntimeException();
    }

    default void removeValue(Ingredient.Value value) {
        throw new RuntimeException();
    }
}
