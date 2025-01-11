package net.player005.recipe_modification;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public record RecipeModifierHolder(ResourceLocation id, RecipeFilter filter, ModificationSet modifications) {
    public void apply(Recipe<?> recipe, ModificationHelper helper) {
        modifications.apply(recipe, helper);
    }
}
