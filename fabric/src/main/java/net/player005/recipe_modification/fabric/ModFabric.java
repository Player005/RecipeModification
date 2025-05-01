package net.player005.recipe_modification.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.player005.recipe_modification.api.RecipeModification;
import net.player005.recipe_modification.impl.Platform_1_21_4;

public class ModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RecipeModification.initPlatform(new Platform_1_21_4() {
            @Override
            public boolean isDevelopmentEnvironment() {
                return FabricLoader.getInstance().isDevelopmentEnvironment();
            }
        });
        ServerLifecycleEvents.SERVER_STARTING
                .register(server -> RecipeModification.onRecipeManagerLoad(server.getRecipeManager()));
    }
}
