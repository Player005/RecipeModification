package net.player005.recipe_modification;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.Nullable;

/**
 * A functional interface to modify the result item of a recipe.
 *
 * @see RecipeModification#registerRecipeResultModifier(Recipe, ResultItemModifier)
 * @see #getResultItem(Recipe, ItemStack, RecipeInput)
 */
@FunctionalInterface
public interface ResultItemModifier {
    /**
     * @param recipe the recipe of which the result item should be modified
     * @param result the current result item
     * @param recipeInput the used RecipeInput when the recipe is assembled, or null when it is only previewed
     * @return the new result item
     */
    ItemStack getResultItem(Recipe<?> recipe, ItemStack result, @Nullable RecipeInput recipeInput);
}
