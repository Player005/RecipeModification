package net.player005.recipe_modification;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Unique;

public interface IngredientAccessor {

    default HolderSet<Item> getValues() {
        throw new IllegalStateException();
    }

    @Unique
    default void replaceValues(HolderSet<Item> newValues) {
        throw new IllegalStateException();
    }
}
