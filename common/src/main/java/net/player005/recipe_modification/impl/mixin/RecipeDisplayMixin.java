package net.player005.recipe_modification.impl.mixin;

import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.*;
import net.player005.recipe_modification.api.RecipeModification;
import net.player005.recipe_modification.impl.RecipeDisplayAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({
    FurnaceRecipeDisplay.class, ShapedCraftingRecipeDisplay.class, ShapelessCraftingRecipeDisplay.class,
    SmithingRecipeDisplay.class, StonecutterRecipeDisplay.class
})
public abstract class RecipeDisplayMixin implements RecipeDisplayAccessor {

    @SuppressWarnings("MixinAnnotationTarget")
    @Inject(at = @At("RETURN"), cancellable = true, target = @Desc(value = "result", ret = SlotDisplay.class))
    public void modifyResult(CallbackInfoReturnable<SlotDisplay> cir) {
        if (recipeModification$parentRecipe == null) return;
        var currentResult = cir.getReturnValue()
            .resolveForFirstStack(new ContextMap.Builder().create(new ContextKeySet.Builder().build()));
        if (currentResult.isEmpty()) return;
        var newResult = RecipeModification.getRecipeResult(recipeModification$parentRecipe, currentResult, null);
        cir.setReturnValue(new SlotDisplay.ItemStackSlotDisplay(newResult));
    }

    @Unique
    public Recipe<?> recipeModification$parentRecipe;

    @Override
    @Unique
    public void recipeModification$setParentRecipe(Recipe<?> recipe) {
        recipeModification$parentRecipe = recipe;
    }
}
