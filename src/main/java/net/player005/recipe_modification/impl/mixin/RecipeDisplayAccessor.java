package net.player005.recipe_modification.impl.mixin;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({
        FurnaceRecipeDisplay.class, ShapedCraftingRecipeDisplay.class, ShapelessCraftingRecipeDisplay.class,
        SmithingRecipeDisplay.class, StonecutterRecipeDisplay.class
})
public interface RecipeDisplayAccessor {
    @SuppressWarnings("MixinAnnotationTarget")
    @Accessor
    void setRecipeModification$parentRecipe(Recipe<?> recipe);
}
