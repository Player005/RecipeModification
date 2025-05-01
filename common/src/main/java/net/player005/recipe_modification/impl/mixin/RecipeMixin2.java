package net.player005.recipe_modification.impl.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.player005.recipe_modification.api.RecipeModification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This modifies the return value of {@link Recipe#assemble(RecipeInput, HolderLookup.Provider)} in order for
 * result modifiers to work.
 */
@Mixin({
        ArmorDyeRecipe.class, BannerDuplicateRecipe.class, BookCloningRecipe.class, DecoratedPotRecipe.class,
        FireworkRocketRecipe.class, FireworkStarFadeRecipe.class, FireworkStarRecipe.class,
        MapCloningRecipe.class, MapExtendingRecipe.class, RepairItemRecipe.class,
        ShapedRecipe.class, ShapelessRecipe.class, ShieldDecorationRecipe.class, SingleItemRecipe.class,
        SmithingTransformRecipe.class, SmithingTrimRecipe.class, TippedArrowRecipe.class, TransmuteRecipe.class
})
public class RecipeMixin2 {
    @SuppressWarnings("MixinAnnotationTarget")
    @Inject(target = @Desc(value = "assemble", args = {RecipeInput.class, HolderLookup.Provider.class}, ret = ItemStack.class),
            at = @At("RETURN"), cancellable = true)
    public void onAssemble(RecipeInput recipeInput, HolderLookup.Provider registries, CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(RecipeModification.getRecipeResult((Recipe<?>) this, cir.getReturnValue(), recipeInput));
    }
}
