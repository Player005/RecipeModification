package net.player005.recipe_modification.unit_tests;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record MockRecipe(ResourceLocation id, ItemStack result, Ingredient... ingredients) implements Recipe<Container> {
    @Override
    public boolean matches(Container input, Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) {
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

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }
}
