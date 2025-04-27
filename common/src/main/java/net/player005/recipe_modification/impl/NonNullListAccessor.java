package net.player005.recipe_modification.impl;

public interface NonNullListAccessor {

    default void recipeModification$makeMutable() {
        throw new RuntimeException();
    }

    default boolean recipeModification$isArrayList() {
        throw new RuntimeException();
    }
}
