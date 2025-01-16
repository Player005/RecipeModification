package net.player005.recipe_modification.unit_tests;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPELESS_RECIPE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Contract(pure = true)
    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, ingredients);
    }
}
