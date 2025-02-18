package net.player005.recipe_modification.impl.mixin;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.player005.recipe_modification.impl.IngredientAccessor;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Predicate;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(Ingredient.class)
public abstract class IngredientMixin implements Predicate<ItemStack>, IngredientAccessor {

    @Shadow @Mutable private Ingredient.Value[] values;

    @Shadow @Nullable private ItemStack[] itemStacks;

    @Shadow @Nullable private IntList stackingIds;

    @Unique
    @Override
    public void replaceValues(Ingredient.Value[] values) {
        this.values = values;
        // wtf idea
        //noinspection DataFlowIssue
        this.itemStacks = null;
        this.stackingIds = null;
    }

    @Unique
    @Override
    public Ingredient.Value[] getValues() {
        return values;
    }

    @Unique
    @Override
    public void removeValue(Ingredient.Value value) {
        replaceValues(ArrayUtils.removeElement(values, value));
    }
}
