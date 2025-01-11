package net.player005.recipe_modification.mixin;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(Ingredient.class)
public abstract class IngredientMixin implements Predicate<ItemStack>, IngredientExtension {

    @Shadow private Ingredient.Value[] values;

    @Shadow @Nullable private ItemStack[] itemStacks;

    @Shadow @Nullable private IntList stackingIds;

    @Override
    public void replaceValues(Ingredient.Value[] values) {
        this.values = values;
        // wtf idea
        //noinspection DataFlowIssue
        this.itemStacks = null;
        this.stackingIds = null;
    }

    @Override
    public Ingredient.Value[] getValues() {
        return values;
    }

    @Override
    public void removeValue(Ingredient.Value value) {
        var values = new ArrayList<>(List.of(this.values));
        values.remove(value);
        //noinspection DataFlowIssue TODO: confirm this works
        replaceValues((Ingredient.Value[]) values.toArray());
    }
}
