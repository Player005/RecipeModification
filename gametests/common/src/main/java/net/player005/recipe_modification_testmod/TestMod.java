package net.player005.recipe_modification_testmod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.player005.recipe_modification.api.RecipeFilter;
import net.player005.recipe_modification.api.RecipeModification;
import net.player005.recipe_modification.api.RecipeModifier;

import java.util.Objects;

public class TestMod {

    public static void init() {
        System.out.println("initializing test mod");
        RecipeModification.registerModifier(
                Objects.requireNonNull(ResourceLocation.tryParse("testmod:test4")),
                RecipeFilter.resultItemIs(Items.ACACIA_DOOR),
                RecipeModifier.replaceResultItem(Items.IRON_DOOR.getDefaultInstance())
        );
    }

}
