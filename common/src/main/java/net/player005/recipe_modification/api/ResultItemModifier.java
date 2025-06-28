package net.player005.recipe_modification.api;

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
     * @param recipe      the recipe of which the result item should be modified
     * @param result      the current result item
     * @param recipeInput the used RecipeInput when the recipe is assembled, or null when it is only previewed
     * @return the new result item stack.
     * <p>
     * <b>Important:</b> The returned stack is passed right to the inventory, so if you are returning
     * a stack from a static field or something like that, make sure to <b>copy()</b> it, otherwise everything explodes!
     */
    ItemStack getResultItem(Recipe<?> recipe, ItemStack result, @Nullable RecipeInput recipeInput);
}
