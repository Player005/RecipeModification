package net.player005.recipe_modification.impl.mixin;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.*;
import net.player005.recipe_modification.api.RecipeModification;
import net.player005.recipe_modification.impl.RecipeManagerAccessorTwo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Mixin(value = RecipeManager.class, priority = 900)
public abstract class RecipeManagerMixin implements RecipeManagerAccessorTwo {

    @Shadow
    private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @Shadow
    protected abstract <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> type);

    @Shadow
    private Map<ResourceLocation, RecipeHolder<?>> byName;

    @Unique
    @Override
    public void recipeModification$makeMutable() {
        byType = MultimapBuilder.hashKeys().arrayListValues().build(byType);
        byName = new HashMap<>(byName);
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;" +
        "Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("RETURN"))
    public void onApply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager,
                        ProfilerFiller profiler, CallbackInfo ci) {
        RecipeModification.onRecipeManagerLoad(((RecipeManager) (Object) this));
    }
}
