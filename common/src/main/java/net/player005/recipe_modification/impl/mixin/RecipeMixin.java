package net.player005.recipe_modification.impl.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.player005.recipe_modification.api.RecipeModification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({
        AbstractCookingRecipe.class, CustomRecipe.class,
        FireworkRocketRecipe.class, FireworkStarRecipe.class,
        ShapedRecipe.class, ShapelessRecipe.class,
        SingleItemRecipe.class,
        SmithingTransformRecipe.class, SmithingTrimRecipe.class,
})
public abstract class RecipeMixin {

    @Inject(method = "getResultItem", at = @At("RETURN"), cancellable = true)
    public void getResultItem(HolderLookup.Provider registries, CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(RecipeModification.getRecipeResult((Recipe<?>) this, cir.getReturnValue(), null));
    }

}
