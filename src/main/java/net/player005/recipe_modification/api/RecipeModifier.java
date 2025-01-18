package net.player005.recipe_modification.api;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.function.Function;

@FunctionalInterface
public interface RecipeModifier {

    void apply(Recipe<?> recipe, RecipeHelper helper);

    /**
     * Removes all ingredients that match the given item.
     */
    static RecipeModifier removeAllIngredients(Item item) {
        return removeIngredients(IngredientSelector.byItem(item));
    }

    /**
     * Tries to remove all ingredients that match the given selector.
     */
    static RecipeModifier removeIngredients(IngredientSelector selector) {
        return (recipe, helper) -> {
            for (var ingredient : selector.selectIngredients(recipe, helper)) {
                recipe.getIngredients().remove(ingredient);
            }
        };
    }

    /**
     * Tries to add the given ingredient to the recipe.
     */
    static RecipeModifier addIngredient(Ingredient ingredient) {
        return (recipe, helper) -> recipe.getIngredients().add(ingredient);
    }

    /**
     * Removes the ingredient at the given index.
     */
    static RecipeModifier removeIngredient(int index) {
        return (recipe, helper) -> recipe.getIngredients().remove(index);
    }

    /**
     * Add the given item as an alternative to matching ingredients.
     */
    static RecipeModifier addAlternative(Item original, Item alternative) {
        return (recipe, helper) -> {
            for (var ingredient : recipe.getIngredients())
                if (ingredient.test(original.getDefaultInstance()))
                    helper.addAlternative(ingredient, alternative);
        };
    }

    /**
     * Add the given tag as an alternative to matching ingredients.
     */
    static RecipeModifier addAlternative(Item original, TagKey<Item> alternative) {
        return (recipe, helper) -> {
            for (var ingredient : recipe.getIngredients())
                if (ingredient.test(original.getDefaultInstance()))
                    helper.addAlternative(ingredient, alternative);
        };
    }

    /**
     * Add the given ingredient as an alternative to matching ingredients.
     */
    static RecipeModifier addAlternative(IngredientSelector selector, Ingredient alternative) {
        return (recipe, helper) -> {
            for (var ingredient : selector.selectIngredients(recipe, helper))
                helper.addAlternative(ingredient, alternative);
        };
    }

    /**
     * Replaces all ingredients that match the given selector with the new ingredient
     */
    static RecipeModifier replaceIngredient(IngredientSelector selector, Ingredient newIngredient) {
        return (recipe, helper) -> {
            var ingredients = selector.selectIngredients(recipe, helper);
            for (var ingredient : ingredients) {
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
