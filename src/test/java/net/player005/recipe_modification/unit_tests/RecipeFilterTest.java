package net.player005.recipe_modification.unit_tests;

import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.player005.recipe_modification.api.RecipeFilter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecipeFilterTest {

    @BeforeEach
    void setUp() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void basicFilterTest() {
        var ingredient1 = Ingredient.of(Items.APPLE, Blocks.FIRE_CORAL, Items.ARROW);
        var ingredient2 = Ingredient.of(Items.ICE, Items.PACKED_ICE);

        var recipe1 = mockRecipeHolder("recipe1", Items.DANDELION.getDefaultInstance(), ingredient1, ingredient1);
        var recipe2 = mockRecipeHolder("recipe2", Items.IRON_AXE.getDefaultInstance(), ingredient2, ingredient2);

        var appleSelector = RecipeFilter.acceptsIngredient(Items.APPLE.getDefaultInstance());
        Assertions.assertTrue(appleSelector.shouldApply(recipe1, null));
        Assertions.assertFalse(appleSelector.shouldApply(recipe2, null));

        var axeResultSelector = RecipeFilter.resultItemIs(Items.IRON_AXE);
        Assertions.assertFalse(axeResultSelector.shouldApply(recipe1, null));
        Assertions.assertTrue(axeResultSelector.shouldApply(recipe2, null));

        var appleAndAxeResultSelector = RecipeFilter.and(appleSelector, axeResultSelector);
        Assertions.assertFalse(appleAndAxeResultSelector.shouldApply(recipe1, null));
        Assertions.assertFalse(appleAndAxeResultSelector.shouldApply(recipe2, null));

        var appleOrAxeResultSelector = RecipeFilter.or(appleSelector, axeResultSelector);
        Assertions.assertTrue(appleOrAxeResultSelector.shouldApply(recipe1, null));
        Assertions.assertTrue(appleOrAxeResultSelector.shouldApply(recipe2, null));

        var idSelector = RecipeFilter.idEquals(ResourceLocation.fromNamespaceAndPath("test", "recipe1"));
        Assertions.assertTrue(idSelector.shouldApply(recipe1, null));
        Assertions.assertFalse(idSelector.shouldApply(recipe2, null));
    }

    @Contract("_, _, _ -> new")
    public static @NotNull RecipeHolder<Recipe<CraftingInput>> mockRecipeHolder(String name, ItemStack resultItem, Ingredient... ingredients) {
        return new RecipeHolder<>(
                ResourceLocation.fromNamespaceAndPath("test", name),
                new MockRecipe(resultItem, ingredients)
        );
    }
}
