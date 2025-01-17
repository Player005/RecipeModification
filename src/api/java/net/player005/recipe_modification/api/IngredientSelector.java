package net.player005.recipe_modification.api;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An object that selects ingredients based on certain criteria when given a recipe.
 * Can easily be used either as a functional interface, or by using the static methods provided by this class.
 */
@FunctionalInterface
public interface IngredientSelector {

    Ingredient[] selectIngredients(Recipe<?> recipe, RecipeHelper helper);

    /**
     * Always select all ingredients of the given recipe.
     */
    IngredientSelector ALL_INGREDIENTS = (recipe, helper) -> recipe.getIngredients().toArray(Ingredient[]::new);

    /**
     * Selects ingredients by their id/ordinal.
     */
    static IngredientSelector byOrdinals(int... numbers) {
        return (recipe, helper) -> Arrays.stream(numbers).mapToObj(recipe.getIngredients()::get).toArray(Ingredient[]::new);
    }

    /**
     * Selects ingredients by their id/ordinal.
     */
    static IngredientSelector byOrdinals(List<Integer> numbers) {
        return (recipe, helper) -> numbers.stream().map(recipe.getIngredients()::get).toArray(Ingredient[]::new);
    }

    /**
     * Selects all ingredients that contain the given item (including tag ingredients that contain the item).
     *
     * @see #matchingItem(Item)
     */
    static IngredientSelector byItem(Item item) {
        return (recipe, helper) -> {
            var toReturn = new ArrayList<Ingredient>();
            for (var ingredient : recipe.getIngredients())
                for (ItemStack ingredientItem : ingredient.getItems())
                    if (ingredientItem.is(item)) toReturn.add(ingredient);
            return toReturn.toArray(Ingredient[]::new);
        };
    }

    /**
     * Selects all ingredients that match exactly the given item (not including tag ingredients that contain the item).
     *
     * @see #byItem(Item)
     */
    static IngredientSelector matchingItem(Item item) {
        return (recipe, helper) -> {
            var toReturn = new ArrayList<Ingredient>();
            for (var ingredient : recipe.getIngredients())
                if (helper.isExactMatch(ingredient, item)) toReturn.add(ingredient);
            return toReturn.toArray(Ingredient[]::new);
        };
    }

    /**
     * Selects all ingredients that match the given tag.
     */
    static IngredientSelector matchingTag(TagKey<Item> tag) {
        return (recipe, helper) -> {
            var toReturn = new ArrayList<Ingredient>();
            for (var ingredient : recipe.getIngredients())
                if (helper.matchesTag(ingredient, tag)) toReturn.add(ingredient);
            return toReturn.toArray(Ingredient[]::new);
        };
    }

    /**
     * Selects all ingredients that match any of the given selectors.
     */
    static IngredientSelector and(IngredientSelector... selectors) {
        return (recipe, helper) -> {
            var toReturn = new ArrayList<Ingredient>();
            for (var selector : selectors) {
                toReturn.addAll(Arrays.asList(selector.selectIngredients(recipe, helper)));
            }
            return toReturn.toArray(Ingredient[]::new);
        };
    }


}
