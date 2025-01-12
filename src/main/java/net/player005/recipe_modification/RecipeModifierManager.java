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
        super(GSON, "recipe_modifier");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        var list = NonNullList.<RecipeModifierHolder>create();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            var id = entry.getKey();

            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                list.add(new RecipeModifierHolder(
                        id,
                        RecipeFilter.Serialization.fromJson(json.get("target_recipes")),
                        ModificationSet.Serialization.fromJson(json.get("modifiers"))
                ));
            } catch (Exception exception) {
                LOGGER.error("Error loading recipe modifier {}:", id, exception);
            }
        }

        RecipeModification.updateJsonRecipeModifiers(list);

        LOGGER.info("Loaded {} recipe modifiers", list.size());
    }
}
