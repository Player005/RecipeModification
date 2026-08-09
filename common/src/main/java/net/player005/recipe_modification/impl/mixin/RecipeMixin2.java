package net.player005.recipe_modification.impl.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.player005.recipe_modification.api.RecipeModification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This modifies the return value of {@link Recipe#assemble(RecipeInput)} in order for
 * result modifiers to work.
 */
@Mixin({
    DyeRecipe.class, BannerDuplicateRecipe.class, BookCloningRecipe.class, DecoratedPotRecipe.class,
    FireworkRocketRecipe.class, FireworkStarFadeRecipe.class, FireworkStarRecipe.class,
    MapExtendingRecipe.class, RepairItemRecipe.class,
    ShapedRecipe.class, ShapelessRecipe.class, ShieldDecorationRecipe.class, SingleItemRecipe.class,
    SmithingTransformRecipe.class, SmithingTrimRecipe.class, TransmuteRecipe.class
})
public class RecipeMixin2 {

    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/RecipeInput;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    public void onAssemble(RecipeInput recipeInput, CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(RecipeModification.getRecipeResult((Recipe<?>) this, cir.getReturnValue(), recipeInput));
    }
}
