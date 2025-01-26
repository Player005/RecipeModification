package net.player005.recipe_modification.unit_tests;

import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record MockRecipe(ItemStack result, Ingredient... ingredients) implements Recipe<CraftingInput> {
    @Override
    public boolean matches(CraftingInput input, Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
        return RecipeSerializer.SHAPELESS_RECIPE;
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<CraftingInput>> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.create(List.of(ingredients));
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return SearchRecipeBookCategory.CRAFTING.includedCategories().getFirst();
    }
}
