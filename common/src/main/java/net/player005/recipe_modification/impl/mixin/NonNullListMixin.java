package net.player005.recipe_modification.impl.mixin;

import net.minecraft.core.NonNullList;
import net.player005.recipe_modification.impl.NonNullListAccessor;
import org.spongepowered.asm.mixin.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This mixin allows trying to make a NonNullList mutable by wrapping the underlying list in an ArrayList.
 * Implements {@link NonNullListAccessor}.
 */
@Mixin(NonNullList.class)
public class NonNullListMixin<E> implements NonNullListAccessor {

    @Mutable
    @Shadow @Final private List<E> list;

    @Unique
    @Override
    public void recipeModification$makeMutable() {
        if (list instanceof ArrayList) return;
        list = new ArrayList<>(list);
    }
}
