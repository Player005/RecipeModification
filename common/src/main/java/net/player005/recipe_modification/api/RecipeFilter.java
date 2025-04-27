package net.player005.recipe_modification.api;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * A simple functional interface to filter recipes.
 * There are some simple filters listed below, but you can easily implement this on your own
 *
 * @see #ALWAYS_APPLY
 * @see #resultItemIs(Item)
 * @see #acceptsIngredient(ItemStack)
 * @see #or(RecipeFilter...)
 * @see #and(RecipeFilter...)
 */
public interface RecipeFilter {

    /**
     * The main (and only) method of a RecipeFilter
     *
     * @param recipe the given recipe to test
     * @return if the test was successful
     */
    boolean shouldApply(RecipeHolder<?> recipe, HolderLookup.Provider registryAccess);

    /**
     * A simple recipe filter that always returns {@code true}.
     */
    RecipeFilter ALWAYS_APPLY = (recipe, registryAccess) -> true;

    /**
     * Returns a recipe filter that filters for recipes that use the given ItemStack as an ingredient
     */
    static RecipeFilter acceptsIngredient(ItemStack item) {
        return (recipe, registryAccess) -> {
            for (var ingredient : recipe.value().getIngredients())
                if (ingredient.test(item)) return true;
            return false;
        };
    }

    /**
     * Returns a recipe filter that filters for recipes that create the given result item.
     */
    static RecipeFilter resultItemIs(Item item) {
        return (recipe, registryAccess) -> recipe.value().getResultItem(registryAccess).is(item);
    }

    /**
     * Returns a recipe filter that filters for recipes that create a result item contained in the given tag.
     */
    static RecipeFilter resultItemIs(TagKey<Item> itemTag) {
        return (recipe, registryAccess) -> recipe.value().getResultItem(registryAccess).is(itemTag);
    }

    /**
     * Returns a recipe filter that filters for the recipe with the given id.
     */
    static RecipeFilter idEquals(ResourceLocation id) {
        return (recipe, registryAccess) -> recipe.id().equals(id);
    }

    /**
     * Returns a recipe filter that filters for recipes in the given namespace.
     */
    static RecipeFilter namespaceEquals(String group) {
        return (recipe, registryAccess) -> recipe.id().getNamespace().equals(group);
    }

    /**
     * Concatenates multiple given filters with a logical and.
     */
    static RecipeFilter and(RecipeFilter... filters) {
        return (recipe, registryAccess) -> {
            for (var filter : filters) if (!filter.shouldApply(recipe, registryAccess)) return false;
            return true;
        };
    }

    /**
     * Concatenates multiple given filters with a logical or.
     */
    static RecipeFilter or(RecipeFilter... filters) {
        return (recipe, registryAccess) -> {
            for (var filter : filters) if (filter.shouldApply(recipe, registryAccess)) return true;
            return false;
        };
    }

    /**
     * Returns a recipe filter that filters for recipes that don't match the given filter (inverts the given filter).
     */
    static RecipeFilter not(RecipeFilter filter) {
        return (recipe, registryAccess) -> !filter.shouldApply(recipe, registryAccess);
    }

}
