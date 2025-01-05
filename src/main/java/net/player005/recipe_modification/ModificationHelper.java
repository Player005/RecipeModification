package net.player005.recipe_modification;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.player005.recipe_modification.mixin.IngredientExtension;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper class for modifying recipes easily.
 *
 * @see RecipeModification#registerModifier(RecipeModifier)
 * @see RecipeModifier
 */
public class ModificationHelper {

    @ApiStatus.Internal
    public static final Map<Recipe<?>, ItemStack> MODIFIED_RESULT_ITEMS = new HashMap<>();

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
    public void addAlternative(Item original, Ingredient.Value alternative) {
        for (Ingredient ingredient : recipeHolder.value().getIngredients()) {
            for (ItemStack item : ingredient.getItems()) {
                if (item.is(original)) addIngredientValue(ingredient, alternative);
            }
        }
    }

    /**
     * Add an alternative to matching items.
     *
     * @param original    the item that can be substituted
     * @param alternative the substitute
     */
    public void addAlternative(Item original, Item alternative) {
        addAlternative(original, new Ingredient.ItemValue(alternative.getDefaultInstance()));
    }

    /**
     * Add an alternative to matching items.
     *
     * @param original    the item that can be substituted
     * @param alternative the substitute
     */
    public void addAlternative(Item original, TagKey<Item> alternative) {
        addAlternative(original, new Ingredient.TagValue(alternative));
    }

    /**
     * Completely replaces the {@link Ingredient#values} of the ingredient with the given one
     * (effectively replacing the entire ingredient)
     */
    public void replaceIngredientValues(Ingredient ingredient, Ingredient.Value[] ingredientValues) {
        ((IngredientExtension) (Object) ingredient).replaceValues(ingredientValues);
    }

    /**
     * Replaces the given ingredient with a new {@link Item} ingredient.
     */
    public void replaceIngredient(Ingredient ingredient, Item item) {
        replaceIngredientValues(ingredient, new Ingredient.ItemValue[]{new Ingredient.ItemValue(item.getDefaultInstance())});
    }

    /**
     * Replaces the given ingredient with a new item tag ingredient.
     */
    public void replaceIngredient(Ingredient ingredient, TagKey<Item> tag) {
        replaceIngredientValues(ingredient, new Ingredient.TagValue[]{new Ingredient.TagValue(tag)});
    }

    /**
     * Replaces the given old ingredient with another ingredient.
     * Under the hood, this just calls {@link #replaceIngredientValues(Ingredient, Ingredient.Value[])}
     * to copy the data from the new ingredient to the existing one.
     */
    public void replaceIngredient(Ingredient old, Ingredient newIngredient) {
        replaceIngredientValues(old, ((IngredientExtension) (Object) newIngredient).getValues());
    }

    /**
     * Removes a given alternative from the ingredient so that it can't be used for the ingredient/recipe anymore.
     * If the given value is the only one in the Ingredient, the recipe might become impossible to make.
     */
    public void removeIngredientValue(Ingredient ingredient, Ingredient.Value toRemove) {
        ((IngredientExtension) (Object) ingredient).removeValue(toRemove);
    }

    /**
     * Adds a {@link Ingredient.Value} to the given ingredient, providing an
     * alternative item to use for the ingredient/recipe.
     */
    public void addIngredientValue(Ingredient ingredient, Ingredient.Value addedValue) {
        var values = ((IngredientExtension) (Object) ingredient).getValues();
        if (ArrayUtils.contains(values, addedValue)) return;

        var newValues = Arrays.copyOf(values, values.length + 1);
        newValues[newValues.length - 1] = addedValue;
        replaceIngredientValues(ingredient, newValues);
    }

    public RecipeHolder<?> getRecipeHolder() {
        return recipeHolder;
    }
}
