package net.player005.recipe_modification_testmod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.player005.recipe_modification.api.RecipeFilter;
import net.player005.recipe_modification.api.RecipeModification;
import net.player005.recipe_modification.api.RecipeModifier;

@Mod("recipe_modification_testmod")
public class TestMod {

    public TestMod(IEventBus eventBus) {
        RecipeModification.registerModifier(
                ResourceLocation.parse("testmod:test4"),
                RecipeFilter.resultItemIs(Items.ACACIA_DOOR),
                RecipeModifier.replaceResultItem(Items.IRON_DOOR.getDefaultInstance())
        );
    }

}
