package net.player005.recipe_modification.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public interface Platform {

    RecipeHolder<?> getRecipeByID(RecipeManager recipeManager, ResourceLocation id);

    RecipeHelper getHelper();
}
