package net.player005.recipe_modification;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RecipeModificationTest {

    @BeforeAll
    static void beforeAll() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void simpleModificationTest() {
        RecipeModification.registerModifier(new RecipeModifier() {
            @Override
            public RecipeFilter getFilter() {
                return RecipeFilter.acceptsIngredient(Items.LEATHER.getDefaultInstance());
            }

            @Override
            public void apply(Recipe<?> recipe, ModificationHelper helper) {
                helper.addAlternative(Items.LEATHER, Items.ACACIA_BUTTON);
            }
        });
    }

    @Test
    void ThrowOnEarlyAccess() {
        Assertions.assertThrowsExactly(IllegalStateException.class, RecipeModification::getRegistryAccess);
        Assertions.assertThrowsExactly(IllegalStateException.class, RecipeModification::getRecipesByResult);
    }

    @AfterAll
    static void afterAll() {

    }
}
