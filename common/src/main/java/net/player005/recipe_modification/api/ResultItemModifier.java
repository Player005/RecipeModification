package net.player005.recipe_modification.api;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A functional interface to modify the result item of a recipe.
 *
 * @see RecipeModification#registerGlobalResultModifier(ResultItemModifier)
 * @see RecipeModification#modifyResultItemSimple(Recipe, Consumer)
 * @see #getResultItem(Recipe, ItemStack, Container)
 */
@FunctionalInterface
public interface ResultItemModifier {
    /**
     * @param result      the current result item
     * @param recipeInput the used RecipeInput when the recipe is assembled, or null when it is only previewed
     * @return the new result item
     */
    ItemStack getResultItem(Recipe<?> recipe, ItemStack result, @Nullable Container recipeInput);
}
