package net.player005.recipe_modification;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(RecipeModification.modID)
public class ModNeoforge {

    public static final String modID = "recipe_modification";

    public ModNeoforge(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class, event -> event.addListener(new RecipeModifierManager()));
        NeoForge.EVENT_BUS.<ServerStartingEvent>addListener(event -> {
            RecipeModification.onRecipeManagerLoad(event.getServer().getRecipeManager());
        });
    }
}
