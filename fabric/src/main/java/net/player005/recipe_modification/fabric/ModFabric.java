package net.player005.recipe_modification.fabric;

import com.google.gson.JsonElement;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.player005.recipe_modification.api.RecipeModification;
import net.player005.recipe_modification.impl.Platform_1_20_1;
import org.jetbrains.annotations.NotNull;

public class ModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RecipeModification.initPlatform(new Platform_1_20_1() {
            @Override
            public boolean isDevelopmentEnvironment() {
                return FabricLoader.getInstance().isDevelopmentEnvironment();
            }

            @Override
            public <T> T parseLootDataType(@NotNull LootDataType<T> lootDataType, @NotNull JsonElement element) {
                return lootDataType.deserialize(null, element).orElseThrow();
            }
        });
    }
}
