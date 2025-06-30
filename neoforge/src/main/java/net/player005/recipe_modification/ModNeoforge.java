package net.player005.recipe_modification;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.player005.recipe_modification.api.RecipeModification;
import net.player005.recipe_modification.impl.Platform_1_21;

@Mod(RecipeModification.modID)
public class ModNeoforge {

    public static final String modID = "recipe_modification";

    public ModNeoforge(@SuppressWarnings("unused") IEventBus modEventBus) {
        RecipeModification.initPlatform(new Platform_1_21());
    }
}
