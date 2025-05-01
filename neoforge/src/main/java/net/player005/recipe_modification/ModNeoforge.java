package net.player005.recipe_modification;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.player005.recipe_modification.api.RecipeModification;
import net.player005.recipe_modification.impl.Platform_1_21_4;
import net.player005.recipe_modification.serialization.RecipeModifierManager;

@Mod(RecipeModification.modID)
public class ModNeoforge {

    public static final String modID = "recipe_modification";

    public ModNeoforge(@SuppressWarnings("unused") IEventBus modEventBus) {
        RecipeModification.initPlatform(new Platform_1_21_4());
        NeoForge.EVENT_BUS.addListener(ServerAboutToStartEvent.class,
            event -> RecipeModification.onRecipeManagerLoad(event.getServer().getRecipeManager()));
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, event -> {
            var rl = ResourceLocation.parse("recipe_modification:recipe_modifier_manager");
            event.addListener(rl, new RecipeModifierManager());
            event.addDependency(rl, ResourceLocation.parse("minecraft:recipe_manager"));
        });
    }
}
