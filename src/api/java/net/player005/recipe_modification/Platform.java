package net.player005.recipe_modification;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Map;

public interface Platform {

    HolderLookup.Provider getRegistryAccess(RecipeManager recipeManager);

    Map<ResourceLocation, RecipeHolder<?>> getRecipesByName(RecipeManager recipeManager);

    RecipeHelper getHelper();
}
