package net.player005.recipe_modification.impl.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.player005.recipe_modification.serialization.RecipeModifierManager;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {

    @ModifyReturnValue(method = "listeners", at = @At("RETURN"))
    public List<PreparableReloadListener> addRecipeModifierListener(List<PreparableReloadListener> original) {
        // init the RecipeModifierManager right before RecipeManager (which was on index 1 previously)
        var newArray = ArrayUtils.insert(1,
                original.toArray(PreparableReloadListener[]::new),
                new RecipeModifierManager());
        return List.of(newArray);
    }
}
