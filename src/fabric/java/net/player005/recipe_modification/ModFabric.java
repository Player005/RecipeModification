package net.player005.recipe_modification;

import net.fabricmc.api.ModInitializer;
import net.player005.recipe_modification.api.RecipeModification;

public class ModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RecipeModification.initPlatform(new Platform_1_21());
    }
}
