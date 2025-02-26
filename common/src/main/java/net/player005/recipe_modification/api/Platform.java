package net.player005.recipe_modification.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

public interface Platform {

    RegistryAccess getRegistryAccess();

    Recipe<?> getRecipeByID(RecipeManager recipeManager, ResourceLocation id);

    RecipeHelper getHelper();

    void removeRecipe(ResourceLocation id);
}
