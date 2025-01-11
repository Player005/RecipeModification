package net.player005.recipe_modification;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

@FunctionalInterface
public interface RecipeModifier {

    void apply(Recipe<?> recipe, ModificationHelper helper);

    /**
     * Removes all ingredients that match the given item.
     */
    static RecipeModifier removeAllIngredients(Item item) {
        return removeIngredients(IngredientSelector.matchingItem(item));
    }

    /**
     * Removes all ingredients that match the given selector.
     */
    static RecipeModifier removeIngredients(IngredientSelector selector) {
        return (recipe, helper) -> {
            for (var ingredient : selector.selectIngredients(recipe)) {
                helper.removeIngredient(ingredient);
            }
        };
    }

    /**
     * Removes the ingredient at the given ordinal.
     */
    static RecipeModifier removeIngredient(int ordinal) {
        return (recipe, helper) -> helper.removeIngredient(recipe.getIngredients().get(ordinal));
    }

    /**
     * Add the given ingredient value as an alternative to matching ingredients.
     */
    static RecipeModifier addAlternative(IngredientSelector selector, Ingredient.Value alternative) {
        return (recipe, helper) -> {
            for (var ingredient : selector.selectIngredients(recipe)) {
                helper.addAlternative(ingredient, alternative);
            }
        };
    }

    /**
     * Add the given item as an alternative to matching ingredients.
     */
    static RecipeModifier addAlternative(Item original, Item alternative) {
        return (recipe, helper) -> helper.addAlternative(original, alternative);
    }

    /**
     * Add the given tag as an alternative to matching ingredients.
     */
    static RecipeModifier addAlternative(Item original, TagKey<Item> alternative) {
        return (recipe, helper) -> helper.addAlternative(original, alternative);
    }

    /**
     * Replaces the ingredient at the given ordinal with the given item.
     */
    static RecipeModifier replaceIngredient(int ordinal, Item item) {
        return (recipe, helper) -> helper.replaceIngredient(recipe.getIngredients().get(ordinal), item);
    }

}
