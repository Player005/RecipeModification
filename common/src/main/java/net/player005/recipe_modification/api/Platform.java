package net.player005.recipe_modification.api;

import com.google.gson.JsonElement;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.player005.recipe_modification.impl.mixin.RecipeManagerAccessor;

public interface Platform {

    RegistryAccess getRegistryAccess();

    Recipe<?> getRecipeByID(RecipeManager recipeManager, ResourceLocation id);

    RecipeHelper getHelper();

    void removeRecipe(RecipeManagerAccessor recipeManager, ResourceLocation id);

    boolean isDevelopmentEnvironment();

    <T> T parseLootDataType(LootDataType<T> lootDataType, JsonElement element);
}
