package net.player005.recipe_modification.impl.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.crafting.RecipeManager;
import net.player005.recipe_modification.api.RecipeModification;
import net.player005.recipe_modification.serialization.RecipeModifierManager;
import org.apache.commons.lang3.ArrayUtils;
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

    @ModifyReturnValue(method = "listeners", at = @At("RETURN"))
    public List<PreparableReloadListener> addRecipeModifierListener(List<PreparableReloadListener> original) {
        var oldArray = original.toArray(PreparableReloadListener[]::new);
        var newArray = ArrayUtils.insert(1, oldArray, new RecipeModifierManager());
        return List.of(newArray);
    }

    @Inject(method = "updateRegistryTags()V", at = @At("RETURN"))
    public void initialiseRecipeModification(CallbackInfo ci) {
        RecipeModification.onRecipeManagerLoad(getRecipeManager());
    }
}
