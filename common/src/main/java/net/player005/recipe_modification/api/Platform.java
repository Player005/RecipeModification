package net.player005.recipe_modification.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;

public interface Platform {

    @Nullable RecipeHolder<?> getRecipeByID(RecipeManager recipeManager, Identifier id);

    RecipeHelper getHelper();

    boolean isDevelopmentEnvironment();
}
