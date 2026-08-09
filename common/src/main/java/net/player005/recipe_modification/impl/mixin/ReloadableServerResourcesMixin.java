package net.player005.recipe_modification.impl.mixin;

import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.player005.recipe_modification.api.RecipeModification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {

    @Shadow
    public abstract RecipeManager getRecipeManager();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void getRegistryAccess(LayeredRegistryAccess<RegistryLayer> fullLayers,
                                  HolderLookup.Provider loadingContext, FeatureFlagSet enabledFeatures,
                                  Commands.CommandSelection commandSelection,
                                  List<Registry.PendingTags<?>> postponedTags,
                                  PermissionSet functionCompilationPermissions,
                                  List<DataComponentInitializers.PendingComponents<?>> newComponents, CallbackInfo ci) {
        RecipeModification.onInitRegistries(loadingContext);
    }


    @Inject(method = "updateComponentsAndStaticRegistryTags", at = @At("RETURN"))
    public void initialiseRecipeModification(CallbackInfo ci) {
        RecipeModification.onRecipeManagerLoad(getRecipeManager());
    }
}
