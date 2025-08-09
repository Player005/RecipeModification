package net.player005.recipe_modification.forge;

import com.google.gson.JsonElement;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.player005.recipe_modification.api.RecipeModification;
import net.player005.recipe_modification.impl.Platform_1_20_1;
import net.player005.recipe_modification.serialization.RecipeModifierManager;
import org.jetbrains.annotations.NotNull;

@Mod(RecipeModification.modID)
public class ModNeoforge {

    public static final String modID = "recipe_modification";

    public ModNeoforge(FMLJavaModLoadingContext context) {
        RecipeModification.initPlatform(new Platform_1_20_1() {
            @Override
            public boolean isDevelopmentEnvironment() {
                return !FMLLoader.isProduction();
            }

            @Override
            public <T> T parseLootDataType(@NotNull LootDataType<T> lootDataType, @NotNull JsonElement element) {
                return lootDataType.deserialize(null, element, ResourceManager.Empty.INSTANCE).orElseThrow();
            }
        });
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterReloadListeners);
    }

    public void onRegisterReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new RecipeModifierManager());
    }
}
