package net.player005.recipe_modification;

import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
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
        RecipeModification.registerModifier(new RecipeModifierHolder(ResourceLocation.fromNamespaceAndPath("recipe_modification", "test"), RecipeFilter.ALWAYS_APPLY, new ModificationSet(new RecipeModifier[]{RecipeModifier.addAlternative(Items.ACACIA_BOAT, Items.ACACIA_BUTTON)})));
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
