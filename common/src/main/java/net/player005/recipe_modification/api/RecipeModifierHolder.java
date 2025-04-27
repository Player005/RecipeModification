package net.player005.recipe_modification.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public record RecipeModifierHolder(ResourceLocation id, RecipeFilter filter, ModificationSet modifications) {

    public RecipeModifierHolder(ResourceLocation id, RecipeFilter filter, RecipeModifier... recipeModifiers) {
        this(id, filter, new ModificationSet(recipeModifiers));
    }

    public void apply(Recipe<?> recipe, RecipeHelper helper) {
        modifications.apply(recipe, helper);
    }
}
