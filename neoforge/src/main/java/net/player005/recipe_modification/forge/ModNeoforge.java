package net.player005.recipe_modification.forge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.player005.recipe_modification.api.RecipeModification;
import net.player005.recipe_modification.impl.Platform_1_20_1;

@Mod(RecipeModification.modID)
public class ModNeoforge {

    public static final String modID = "recipe_modification";

    public ModNeoforge() {
        RecipeModification.initPlatform(new Platform_1_20_1() {
            @Override
            public boolean isDevelopmentEnvironment() {
                return !FMLLoader.isProduction();
            }
        });
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
    }

    private void onServerStarting(ServerStartingEvent event) {
        //((TestIngredientExtension) getRecipeManager(event.getServer()).getRecipes().stream().findFirst().get().getIngredients().get(0)).exampleMethod();
        RecipeModification.onRecipeManagerLoad(getRecipeManager(event.getServer()));
    }

    private RecipeManager getRecipeManager(MinecraftServer server) {
        // remapping seems to be broken for MinecraftServer#getRecipeManager, so we have to do this instead
        return server.getServerResources().managers().getRecipeManager();
    }
}
