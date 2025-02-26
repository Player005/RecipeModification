package net.player005.recipe_modification.impl.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.flag.FeatureFlagSet;
import net.player005.recipe_modification.impl.Platform_1_20_1;
import net.player005.recipe_modification.serialization.RecipeModifierManager;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    public void getRegistryAccess(RegistryAccess.Frozen registryAccess, FeatureFlagSet enabledFeatures,
                                  Commands.CommandSelection commandSelection, int functionCompilationLevel,
                                  CallbackInfo ci) {
        Platform_1_20_1.REGISTRY_ACCESS = registryAccess;
    }

    @ModifyReturnValue(method = "listeners", at = @At("RETURN"))
    public List<PreparableReloadListener> addRecipeModifierListener(List<PreparableReloadListener> original) {
        var newArray = ArrayUtils.insert(1,
                original.toArray(PreparableReloadListener[]::new),
                new RecipeModifierManager());
        return List.of(newArray);
    }
}
