package net.player005.recipe_modification.api;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public interface Platform {

    HolderLookup.Provider getRegistryAccess(RecipeManager recipeManager);

    RecipeHolder<?> getRecipeByID(RecipeManager recipeManager, ResourceLocation id);

    RecipeHelper getHelper();
}
