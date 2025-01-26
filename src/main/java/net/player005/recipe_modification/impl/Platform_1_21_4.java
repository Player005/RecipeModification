package net.player005.recipe_modification.impl;

import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.player005.recipe_modification.api.Platform;
import net.player005.recipe_modification.api.RecipeHelper;
import net.player005.recipe_modification.impl.mixin.RecipeManagerAccessor;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NotNullByDefault
public class Platform_1_21_4 implements Platform {
    @Override
    public HolderLookup.Provider getRegistryAccess(RecipeManager recipeManager) {
        return ((RecipeManagerAccessor) recipeManager).getRegistries();
    }

    @Override
    public RecipeHolder<?> getRecipeByID(RecipeManager recipeManager, ResourceLocation id) {
        return Objects.requireNonNull(((RecipeManagerAccessor) recipeManager).getRecipes().byKey(ResourceKey.create(
                Registries.RECIPE, id
        )));
    }

    @Override
    public RecipeHelper getHelper() {
        return RecipeHelper_1_21_4.INSTANCE;
    }

    @NotNullByDefault
    public static class RecipeHelper_1_21_4 implements RecipeHelper {

        static final RecipeHelper_1_21_4 INSTANCE = new RecipeHelper_1_21_4();

        public static List<Holder<Item>> asHolderList(Item[] items) {
            return Arrays.stream(items).map(Holder::direct).toList();
        }

        public IngredientAccessor getAccessor(Ingredient ingredient) {
            return (IngredientAccessor) (Object) ingredient;
        }

        public Stream<Holder<Item>> getHolderStream(Ingredient ingredient) {
            return getAccessor(ingredient).getValues().stream();
        }

        public void replaceValues(Ingredient ingredient, List<Holder<Item>> holderList) {
            getAccessor(ingredient).replaceValues(HolderSet.direct(holderList));
        }

        @Override
        public void addAlternative(Ingredient ingredient, Item... items) {
            var addedItems = asHolderList(items);
            var newValues = getHolderStream(ingredient).collect(Collectors.toList());
            newValues.addAll(addedItems);

            getAccessor(ingredient).replaceValues(HolderSet.direct(newValues));
        }

        @Override
        public void addAlternative(Ingredient ingredient, TagKey<Item> itemTag) {
            var addedItems = Lists.newArrayList(BuiltInRegistries.ITEM.getTagOrEmpty(itemTag));

            var newValues = getHolderStream(ingredient).collect(Collectors.toList());
            newValues.addAll(addedItems);
            getAccessor(ingredient).replaceValues(HolderSet.direct(newValues));
        }

        @Override
        public void addAlternative(Ingredient ingredient, Ingredient alternative) {
            var addedValues = getHolderStream(ingredient).toList();
            var newValues = getHolderStream(ingredient).collect(Collectors.toList());
            newValues.addAll(addedValues);

            getAccessor(ingredient).replaceValues(HolderSet.direct(newValues));
        }

        @Override
        public void removeAlternatives(Ingredient ingredient, Item... items) {
            var values = getHolderStream(ingredient).collect(Collectors.toSet());

            values.removeIf(itemHolder -> ArrayUtils.contains(items, itemHolder.value()));
            //noinspection unchecked
            getAccessor(ingredient).replaceValues(HolderSet.direct(values.toArray(Holder[]::new)));
        }

        @Override
        public void removeAlternative(Ingredient ingredient, TagKey<Item> itemTag) {
            var values = getHolderStream(ingredient).collect(Collectors.toSet());

            values.removeIf(item -> item.is(itemTag));
            //noinspection unchecked
            getAccessor(ingredient).replaceValues(HolderSet.direct(values.toArray(Holder[]::new)));
        }

        @Override
        public void replaceIngredient(Ingredient ingredient, Ingredient newIngredient) {
            getAccessor(ingredient).replaceValues(getAccessor(newIngredient).getValues());
        }

        @Override
        public boolean isExactMatch(Ingredient ingredient, Item item) {
            return getAccessor(ingredient).getValues().size() == 1 && ingredient.test(item.getDefaultInstance());
        }

        @Override
        public boolean matchesTag(Ingredient ingredient, TagKey<Item> tag) {
            var values = getHolderStream(ingredient).toList();

            for (var itemHolder : BuiltInRegistries.ITEM.getTagOrEmpty(tag))
                if (!values.contains(itemHolder)) return false;

            return true;
        }
    }
}
