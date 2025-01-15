package net.player005.recipe_modification.serialization;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.player005.recipe_modification.RecipeModification;
import net.player005.recipe_modification.RecipeModifierHolder;
import org.slf4j.Logger;

import java.util.Map;

public class RecipeModifierManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogUtils.getLogger();

    public RecipeModifierManager() {
        super(GSON, "recipe_modifier");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        var builder = ImmutableList.<RecipeModifierHolder>builder();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            var id = entry.getKey();

            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                builder.add(new RecipeModifierHolder(
                        id,
                        RecipeFilterSerializer.fromJson(json.get("target_recipes")),
                        ModificationSetSerializer.fromJson(json.get("modifiers"))
                ));
            } catch (Exception exception) {
                LOGGER.error("Error loading recipe modifier {}:", id, exception);
            }
        }
        var list = builder.build();

        RecipeModification.updateJsonRecipeModifiers(list);
        LOGGER.info("Loaded {} recipe modifiers", list.size());
    }
}
