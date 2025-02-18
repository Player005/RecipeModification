package net.player005.recipe_modification.impl;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.player005.recipe_modification.api.Platform;
import net.player005.recipe_modification.api.RecipeHelper;
import net.player005.recipe_modification.api.RecipeModification;
import net.player005.recipe_modification.impl.mixin.ItemValueAccessor;
import net.player005.recipe_modification.impl.mixin.RecipeManagerAccessor;
import net.player005.recipe_modification.impl.mixin.TagValueAccessor;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

@NotNullByDefault
public class Platform_1_20_1 implements Platform {

    @ApiStatus.Internal
    public static RegistryAccess.@UnknownNullability Frozen REGISTRY_ACCESS;

    @Override
    public RegistryAccess getRegistryAccess() {
        return REGISTRY_ACCESS;
    }

    @Override
    public Recipe<?> getRecipeByID(RecipeManager recipeManager, ResourceLocation id) {
        return ((RecipeManagerAccessor) recipeManager).getByName().get(id);
    }

    @Override
    public RecipeHelper getHelper() {
        return RecipeHelper_1_20_1.INSTANCE;
    }

    @Override
    public void removeRecipe(ResourceLocation id) {
        var recipeManager = (RecipeManagerAccessor) RecipeModification.getRecipeManager();
        recipeManager.getByName().remove(id);
        for (var map : recipeManager.getByType().values()) {
            map.remove(id);
        }
    }

    public static class RecipeHelper_1_20_1 implements RecipeHelper {

        static RecipeHelper INSTANCE = new RecipeHelper_1_20_1();

        public static IngredientAccessor getAccessor(Ingredient ingredient) {
            return (IngredientAccessor) (Object) ingredient;
        }

        public static void replaceIngredientValues(Ingredient ingredient, Ingredient.Value[] values) {
            getAccessor(ingredient).replaceValues(values);
        }

        @Override
        public void addAlternative(Ingredient ingredient, Item... items) {
            var newValues = ArrayUtils.addAll(getAccessor(ingredient).getValues(), (Ingredient.Value[]) items);
            getAccessor(ingredient).replaceValues(newValues);
        }

        @Override
        public void addAlternative(Ingredient ingredient, TagKey<Item> itemTag) {
            replaceIngredientValues(ingredient, ArrayUtils.add(getAccessor(ingredient).getValues(),
                    new Ingredient.TagValue(itemTag)));
        }

        @Override
        public void addAlternative(Ingredient ingredient, Ingredient alternative) {
            replaceIngredientValues(ingredient, ArrayUtils.addAll(getAccessor(ingredient).getValues(),
                    getAccessor(alternative).getValues()));
        }

        @Override
        public void removeAlternatives(Ingredient ingredient, Item... items) {
            var newValues = new ArrayList<>(List.of(getAccessor(ingredient).getValues()));
            for (var item : items)
                newValues.removeIf(value -> value instanceof Ingredient.ItemValue && ((ItemValueAccessor) value).getItem().is(item));
            getAccessor(ingredient).replaceValues(newValues.toArray(new Ingredient.Value[0]));
        }

        @Override
        public void removeAlternative(Ingredient ingredient, TagKey<Item> itemTag) {
            var newValues = new ArrayList<>(List.of(getAccessor(ingredient).getValues()));
            newValues.removeIf(value -> value instanceof Ingredient.TagValue && itemTag.equals(((TagValueAccessor) value).getTag()));
            getAccessor(ingredient).replaceValues(newValues.toArray(new Ingredient.Value[0]));
        }

        @Override
        public void replaceIngredient(Ingredient ingredient, Ingredient newIngredient) {
            replaceIngredientValues(ingredient, getAccessor(newIngredient).getValues());
        }

        @Override
        public boolean isExactMatch(Ingredient ingredient, Item item) {
            for (Ingredient.Value value : getAccessor(ingredient).getValues())
                if (value instanceof Ingredient.ItemValue && ((ItemValueAccessor) value).getItem().is(item))
                    return true;
            return false;
        }

        @Override
        public boolean matchesTag(Ingredient ingredient, TagKey<Item> tag) {
            for (Ingredient.Value value : getAccessor(ingredient).getValues())
                if (value instanceof Ingredient.TagValue && ((TagValueAccessor) value).getTag().equals(tag))
                    return true;
            return false;
        }
    }
}
