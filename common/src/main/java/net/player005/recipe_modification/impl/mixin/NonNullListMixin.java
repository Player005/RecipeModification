package net.player005.recipe_modification.impl.mixin;

import net.minecraft.core.NonNullList;
import net.player005.recipe_modification.impl.NonNullListAccessor;
import org.spongepowered.asm.mixin.*;

import java.util.ArrayList;
import java.util.List;

@Mixin(NonNullList.class)
public class NonNullListMixin<E> implements NonNullListAccessor {

    @Mutable
    @Shadow @Final private List<E> list;

    @Unique
    @Override
    public void recipeModification$makeMutable() {
        list = new ArrayList<>(list);
    }

    @Override
    public boolean recipeModification$isArrayList() {
        return list instanceof ArrayList;
    }
}
