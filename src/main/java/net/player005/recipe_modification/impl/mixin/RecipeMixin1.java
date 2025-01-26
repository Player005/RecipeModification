package net.player005.recipe_modification.impl.mixin;

import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin({
        AbstractCookingRecipe.class, ShapedRecipe.class, ShapelessRecipe.class,
        SmithingTransformRecipe.class, SmithingTrimRecipe.class, StonecutterRecipe.class, TransmuteRecipe.class
})
public class RecipeMixin1 {
    @SuppressWarnings("MixinAnnotationTarget")
    @Inject(at = @At("RETURN"), target = @Desc(value = "display", ret= List.class))
    public void modifyDisplay(CallbackInfoReturnable<List<RecipeDisplay>> cir) {
        for (var display : cir.getReturnValue()) {
            ((RecipeDisplayAccessor) display).setRecipeModification$parentRecipe((Recipe<?>) this);
        }
    }
}
