package net.player005.recipe_modification.impl.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.player005.recipe_modification.impl.NonNullListAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * This Mixin makes the ingredient list mutable in order for {@code add_ingredient} and {@code remove_ingredient} to
 * work.
 */
@Mixin(ShapelessRecipe.class)
public class RecipeMixin3 {

    @ModifyVariable(method = "<init>", at = @At("CTOR_HEAD"), argsOnly = true)
    private List<Ingredient> mutableIngredientList(List<Ingredient> value) {
        if (value instanceof ArrayList) return value;
        if (value instanceof NonNullList<?>) {
            ((NonNullListAccessor) value).recipeModification$makeMutable();
            return value;
        }
        return new ArrayList<>(value);
    }
}
