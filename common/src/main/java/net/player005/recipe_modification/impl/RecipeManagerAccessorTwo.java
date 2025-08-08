package net.player005.recipe_modification.impl;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface RecipeManagerAccessorTwo {

    default void recipeModification$makeMutable() {
        throw new RuntimeException();
    }
}