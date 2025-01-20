package net.player005.recipe_modification.mixin;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.player005.recipe_modification.IngredientAccessor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Ingredient.class)
public abstract class IngredientAccessorInjector implements IngredientAccessor {

    @Override @Accessor public abstract HolderSet<Item> getValues();

    @Shadow @Mutable @Final private HolderSet<Item> values;

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Unique
    @Override
    public void replaceValues(HolderSet<Item> newValues) {
        values = newValues;
    }
}
