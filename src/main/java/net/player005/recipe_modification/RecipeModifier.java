package net.player005.recipe_modification;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.function.Function;

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
        return (recipe, helper) -> helper.addAlternative(selector, alternative);
    }

    /**
     * Add the given item as an alternative to matching ingredients.
     */
    static RecipeModifier addAlternative(Item original, Item alternative) {
        return (recipe, helper) -> helper.addAlternative(IngredientSelector.matchingItem(original),
                new Ingredient.ItemValue(alternative.getDefaultInstance()));
    }

    /**
     * Add the given tag as an alternative to matching ingredients.
     */
    static RecipeModifier addAlternative(Item original, TagKey<Item> alternative) {
        return (recipe, helper) -> helper.addAlternative(IngredientSelector.matchingItem(original),
                new Ingredient.TagValue(alternative));
    }

    /**
     * Replaces all ingredients that match the given selector with the new ingredient
     */
    static RecipeModifier replaceIngredient(IngredientSelector selector, Ingredient newIngredient) {
        return (recipe, helper) -> {
            for (var ingredient : selector.selectIngredients(recipe)) {
                helper.replaceIngredient(ingredient, newIngredient);
            }
        };
    }

    static RecipeModifier modifyResultItem(Function<ItemStack, ItemStack> modifier) {
        return (recipe, helper) -> RecipeModification.registerRecipeResultModifier(recipe, (recipe1, result, recipeInput) -> modifier.apply(result));
    }

    static RecipeModifier replaceResultItem(ItemStack newResult) {
        return modifyResultItem(stack -> newResult);
    }

    static RecipeModifier addResultComponents(DataComponentPatch patch) {
        return modifyResultItem(stack -> {
            stack.applyComponents(patch);
            return stack;
        });
    }
}
