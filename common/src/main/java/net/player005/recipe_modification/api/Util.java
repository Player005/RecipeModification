package net.player005.recipe_modification.api;

import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public class Util {

    public static @Nullable ItemStack getResultItem(Recipe<?> recipe) {
        for (var recipeDisplay : recipe.display()) {
            return recipeDisplay.result().resolveForFirstStack(
                new ContextMap.Builder().create(new ContextKeySet.Builder().build())
            );
        }
        return null;
    }

    public static @Nullable ItemStack getResultItem(RecipeHolder<?> recipeHolder) {
        return getResultItem(recipeHolder.value());
    }
}
