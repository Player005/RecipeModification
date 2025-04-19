package net.player005.recipe_modification.unit_tests;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.player005.recipe_modification.api.IngredientSelector;
import net.player005.recipe_modification.impl.Platform_1_21;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.player005.recipe_modification.unit_tests.RecipeFilterTest.mockRecipeHolder;

public class IngredientSelectorTest {
    @BeforeEach
    void setUp() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void test() {
        var appleIngredient = Ingredient.of(Items.APPLE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
        var iceIngredient = Ingredient.of(Items.ICE, Items.PACKED_ICE);

        var recipe1 = mockRecipeHolder("recipe1", Items.DANDELION.getDefaultInstance(), appleIngredient, appleIngredient);
        var recipe2 = mockRecipeHolder("recipe2", Items.IRON_AXE.getDefaultInstance(), iceIngredient);

        var helper = new Platform_1_21.RecipeHelper_1_21();

        var gAppleSelector = IngredientSelector.byItem(Items.GOLDEN_APPLE);
        Assertions.assertEquals(2, gAppleSelector.selectIngredients(recipe1.value(), helper).length);
        Assertions.assertEquals(0, gAppleSelector.selectIngredients(recipe2.value(), helper).length);
    }
}
