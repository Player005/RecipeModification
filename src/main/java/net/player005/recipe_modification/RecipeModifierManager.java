package net.player005.recipe_modification;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.Map;

public class RecipeModifierManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogUtils.getLogger();

    public RecipeModifierManager() {
        super(GSON, "recipe_modifiers");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        var list = NonNullList.<RecipeModifierHolder>create();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            var id = entry.getKey();

            try {
                list.add(deserializeModifier(id, entry.getValue().getAsJsonObject()));
            } catch (Exception exception) {
                LOGGER.error("Error loading recipe modifier {}:", id, exception);
            }
        }

        RecipeModification.updateJsonRecipeModifiers(list);
    }

    private RecipeModifierHolder deserializeModifier(ResourceLocation id, JsonObject json) {
        return new RecipeModifierHolder(id, deserializeRecipeFilter(json.get("target_recipes")), deserializeModifiers(json.get("modifiers")));
    }

    private ModificationSet deserializeModifiers(JsonElement modifiers) {
        return ModificationSet.Serialization.fromJson(modifiers);
    }

    private RecipeFilter deserializeRecipeFilter(JsonElement targetRecipes) {
        return RecipeFilter.Serialization.fromJson(targetRecipes);
    }
}
