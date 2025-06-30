package net.player005.recipe_modification.impl;

public interface RecipeManagerAccessorTwo {

    default void recipeModification$makeMutable() {
        throw new RuntimeException();
    }
}