package net.player005.recipe_modification;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.player005.recipe_modification.api.RecipeModification;

public class ModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RecipeModification.initPlatform(new Platform_1_21());
        ServerLifecycleEvents.SERVER_STARTING
                .register(server -> RecipeModification.onRecipeManagerLoad(server.getRecipeManager()));
    }
}
