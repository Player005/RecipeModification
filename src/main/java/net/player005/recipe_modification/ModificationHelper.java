package net.player005.recipe_modification;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.player005.recipe_modification.mixin.IngredientAccessor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * A helper class for modifying recipes easily.
 *
 * @see RecipeModification#registerModifier(RecipeModifierHolder)
 * @see RecipeModifierHolder
 */
public class ModificationHelper {

    private final RecipeHolder<?> recipeHolder;

    /**
     * Constructs a new recipe modification helper.
     */
    public ModificationHelper(RecipeHolder<?> recipe) {
        this.recipeHolder = recipe;
    }

    /**
     * Tries to remove the given ingredient from the recipe.
     * Doesn't work with all recipe types (like cooking/smelting and stonecutting)
     */
    public void removeIngredient(Ingredient ingredient) {
        recipeHolder.value().getIngredients().remove(ingredient);
    }

    /**
     * Tries to add another ingredient to the recipe.
     * Doesn't work with all recipe types (like cooking/smelting and stonecutting)
     */
    public void addIngredient(Ingredient ingredient) {
        recipeHolder.value().getIngredients().add(ingredient);
    }

    /**
     * Add an alternative to matching items.
     *
     * @param original    the item that can be substituted
     * @param alternative the substitute
     */
    public void addAlternative(Ingredient original, Ingredient.Value alternative) {
        addIngredientValue(original, alternative);
    }

    /**
     * Add an alternative to matching items.
     *
     * @param selector    {@link IngredientSelector} for the item that can be substituted
     * @param alternative the substitute
     */
    public void addAlternative(IngredientSelector selector, Ingredient.Value alternative) {
        for (var ingredient : selector.selectIngredients(recipeHolder.value())) {
            addAlternative(ingredient, alternative);
        }
    }

    /**
     * Completely replaces the {@link Ingredient#values} of the ingredient with the given one
     * (effectively replacing the entire ingredient)
     *
     * @see #replaceIngredientValues(IngredientSelector, Ingredient.Value[])
     */
    public void replaceIngredientValues(Ingredient ingredient, Ingredient.Value[] ingredientValues) {
        ((IngredientAccessor) (Object) ingredient).replaceValues(ingredientValues);
    }

    /**
     * Completely replaces the {@link Ingredient#values} of all ingredients selected by the given selector with the given one
     * (effectively replacing the entire ingredients)
     *
     * @see #replaceIngredientValues(Ingredient, Ingredient.Value[])
     */
    public void replaceIngredientValues(IngredientSelector ingredientSelector, Ingredient.Value[] newValues) {
        for (var ingredient : ingredientSelector.selectIngredients(recipeHolder.value())) {
            ((IngredientAccessor) (Object) ingredient).replaceValues(newValues);
        }
    }

    /**
     * Replaces the given old ingredient with another ingredient.
     * Under the hood, this just calls {@link #replaceIngredientValues(Ingredient, Ingredient.Value[])}
     * to copy the data from the new ingredient to the existing one.
     */
    public void replaceIngredient(Ingredient old, Ingredient newIngredient) {
        replaceIngredientValues(old, ((IngredientAccessor) (Object) newIngredient).getValues());
    }

    /**
     * Removes a given alternative from the ingredient so that it can't be used for the ingredient/recipe anymore.
     * If the given value is the only one in the Ingredient, the recipe might become impossible to make.
     */
    public void removeIngredientValue(Ingredient ingredient, Ingredient.Value toRemove) {
        ((IngredientAccessor) (Object) ingredient).removeValue(toRemove);
    }

    /**
     * Adds an {@link Ingredient.Value} to the given ingredient, providing an
     * alternative item to use for the ingredient/recipe.
     */
    public void addIngredientValue(Ingredient ingredient, Ingredient.Value addedValue) {
        var values = ((IngredientAccessor) (Object) ingredient).getValues();
        if (ArrayUtils.contains(values, addedValue)) return;

        var newValues = Arrays.copyOf(values, values.length + 1);
        newValues[newValues.length - 1] = addedValue;
        replaceIngredientValues(ingredient, newValues);
    }

    public RecipeHolder<?> getRecipeHolder() {
        return recipeHolder;
    }
}
