package net.player005.recipe_modification;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.player005.recipe_modification.api.RecipeModification;

@Mod(RecipeModification.modID)
public class ModNeoforge {

    public static final String modID = "recipe_modification";

    public ModNeoforge(@SuppressWarnings("unused") IEventBus modEventBus) {
        RecipeModification.initPlatform(new Platform_1_21());
        NeoForge.EVENT_BUS.addListener(ServerAboutToStartEvent.class,
                event -> RecipeModification.onRecipeManagerLoad(event.getServer().getRecipeManager()));
    }
}
