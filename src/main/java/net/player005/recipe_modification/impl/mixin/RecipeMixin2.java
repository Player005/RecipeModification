package net.player005.recipe_modification.impl.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.player005.recipe_modification.api.RecipeModification;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({
        AbstractCookingRecipe.class, ArmorDyeRecipe.class, BannerDuplicateRecipe.class,
        BookCloningRecipe.class, DecoratedPotRecipe.class,
        FireworkRocketRecipe.class, FireworkStarFadeRecipe.class, FireworkStarRecipe.class,
        MapCloningRecipe.class, MapExtendingRecipe.class, RepairItemRecipe.class,
        ShapedRecipe.class, ShapelessRecipe.class, ShieldDecorationRecipe.class,
        ShulkerBoxColoring.class, SingleItemRecipe.class,
        SmithingTransformRecipe.class, SmithingTrimRecipe.class, SuspiciousStewRecipe.class,
        TippedArrowRecipe.class
})
public class RecipeMixin2 {

    @SuppressWarnings("MixinAnnotationTarget")
    @Inject(at = @At("RETURN"), cancellable = true, target = @Desc(value = "assemble", args =
            {Container.class, RegistryAccess.class}, ret = ItemStack.class), require = 0)
    public void modifyAssemble(CallbackInfoReturnable<ItemStack> cir, @Local(argsOnly = true) Container recipeInput) {
        cir.setReturnValue(RecipeModification.getRecipeResult((Recipe<?>) this, cir.getReturnValue(), recipeInput).copy());
    }

}
