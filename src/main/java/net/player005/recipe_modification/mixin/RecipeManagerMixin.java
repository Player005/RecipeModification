package net.player005.recipe_modification.mixin;

import net.minecraft.world.item.crafting.RecipeManager;
import net.player005.recipe_modification.RecipeModification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Inject(method = {"<init>", "replaceRecipes"}, at = @At("RETURN"))
    void onRecipeManagerLoad(CallbackInfo ci) {
        RecipeModification.onRecipeManagerLoad((RecipeManager) (Object) this);
    }
}
