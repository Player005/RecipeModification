package net.player005.recipe_modification;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.player005.recipe_modification.mixin.IngredientAccessor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * An object that selects ingredients based on certain criteria when given a recipe.
 * Can easily be used either as a functional interface, or by using the static methods provided by this class.
 */
@FunctionalInterface
public interface IngredientSelector {

    Ingredient[] selectIngredients(Recipe<?> recipe);

    /**
     * Always select all ingredients of the given recipe.
     */
    IngredientSelector ALL_INGREDIENTS = recipe -> recipe.getIngredients().toArray(Ingredient[]::new);

    /**
     * Selects ingredients by their id/ordinal.
     */
    static IngredientSelector fromOrdinals(int... numbers) {
        return recipe -> Arrays.stream(numbers).mapToObj(recipe.getIngredients()::get).toArray(Ingredient[]::new);
    }

    /**
     * Selects all ingredients that contain the given item.
     */
    static IngredientSelector allForItem(Item items) {
        return recipe -> {
            var toReturn = new ArrayList<Ingredient>();
            for (var ingredient : recipe.getIngredients()) {
                if (ArrayUtils.contains(ingredient.getItems(), items)) toReturn.add(ingredient);
            }
            return toReturn.toArray(Ingredient[]::new);
        };
    }

    /**
     * Selects all ingredients that match exactly the given item (not including tag ingredients that contain the item).
     */
    static IngredientSelector matchingItem(Item item) {
        return recipe -> {
            var toReturn = new ArrayList<Ingredient>();
            for (var ingredient : recipe.getIngredients()) {
                for (var value : ((IngredientAccessor) (Object) ingredient).getValues()) {
                    if (value instanceof Ingredient.ItemValue(ItemStack ingredientStack) && ingredientStack.is(item))
                        toReturn.add(ingredient);
                }
            }
            return toReturn.toArray(Ingredient[]::new);
        };
    }

    /**
     * Selects all ingredients that match the given tag.
     */
    static IngredientSelector matchingTag(TagKey<Item> tag) {
        return recipe -> {
            var toReturn = new ArrayList<Ingredient>();
            for (var ingredient : recipe.getIngredients()) {
                for (var value : ((IngredientAccessor) (Object) ingredient).getValues()) {
                    if (value instanceof Ingredient.TagValue(TagKey<Item> ingredientTag) && ingredientTag.equals(tag))
                        toReturn.add(ingredient);
                }
            }
            return toReturn.toArray(Ingredient[]::new);
        };
    }
}
