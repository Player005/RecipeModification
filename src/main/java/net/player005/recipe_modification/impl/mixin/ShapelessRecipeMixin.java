package net.player005.recipe_modification.impl.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.player005.recipe_modification.impl.NonNullListAccessor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeMixin {

    @Shadow @Final
    NonNullList<Ingredient> ingredients;

    @Inject(method = "getIngredients", at = @At("RETURN"))
    public void getIngredients(CallbackInfoReturnable<Ingredient[]> cir) {
        var accessor = (NonNullListAccessor) ingredients;
        if (!accessor.recipeModification$isArrayList()) {
            accessor.recipeModification$makeMutable();
        }
    }
}
