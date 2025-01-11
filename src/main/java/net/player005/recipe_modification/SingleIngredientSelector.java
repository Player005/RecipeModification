package net.player005.recipe_modification;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public interface SingleIngredientSelector extends IngredientSelector {

    Ingredient selectIngredient(Recipe<?> recipe);

    @Override
    default Ingredient[] selectIngredients(Recipe<?> recipe) {
        return new Ingredient[]{selectIngredient(recipe)};
    }

    static SingleIngredientSelector fromOrdinal(int ordinal) {
        return recipe -> recipe.getIngredients().get(ordinal);
    }
}
