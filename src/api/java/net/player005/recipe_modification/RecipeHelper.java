package net.player005.recipe_modification;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public interface RecipeHelper {

    void addAlternative(Ingredient ingredient, Item... items);

    void addAlternative(Ingredient ingredient, TagKey<Item> itemTag);

    void addAlternative(Ingredient ingredient, Ingredient alternative);

    void removeAlternatives(Ingredient ingredient, Item... items);

    void removeAlternative(Ingredient ingredient, TagKey<Item> itemTag);

    void replaceIngredient(Ingredient ingredient, Ingredient newIngredient);

    boolean isExactMatch(Ingredient ingredient, Item item);

    boolean matchesTag(Ingredient ingredient, TagKey<Item> tag);
}
