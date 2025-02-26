package net.player005.recipe_modification.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.player005.recipe_modification.api.RecipeFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class RecipeFilterSerializer {
    private static final Map<String, Function<JsonObject, RecipeFilter>> deserializers = new HashMap<>();

    public static RecipeFilter fromJson(JsonElement json) {
        if (json instanceof JsonPrimitive primitive && primitive.isString())
            return fromString(primitive.getAsString());

        else if (!json.isJsonObject())
            throw new RecipeModifierParsingException("Invalid recipe filter: expected an object");
        var object = json.getAsJsonObject();

        var filterId = object.get("type").getAsString();
        if (!deserializers.containsKey(filterId))
            throw new RecipeModifierParsingException("Unknown recipe filter type: " + filterId);

        return deserializers.get(filterId).apply(object);
    }

    private static RecipeFilter fromString(String string) {
        return RecipeFilter.idEquals(Objects.requireNonNull(ResourceLocation.tryParse(string)));
    }

    static {
        registerSerializer("all_recipes", (json) -> RecipeFilter.ALWAYS_APPLY);
        registerSerializer("accepting_ingredient", (json) -> {
            var item = getItemStack(json.get("item"), err -> {
                throw new RecipeModifierParsingException("Invalid recipe filter: " + err);
            });
            return RecipeFilter.acceptsIngredient(item);
        });
        registerSerializer("result_item_is", (json) -> {
            var item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(json.get("item").getAsString()));
            return RecipeFilter.resultItemIs(item);
        });
        registerSerializer("id_equals", (json) -> {
            var id = ResourceLocation.tryParse(json.get("id").getAsString());
            if (id == null)
                throw new RecipeModifierParsingException("Invalid recipe filter: Invalid id: \"" + json.get("id") + "\"");
            return RecipeFilter.idEquals(id);
        });
        registerSerializer("namespace_equals", (json) -> {
            var namespace = json.get("namespace").getAsString();
            return RecipeFilter.namespaceEquals(namespace);
        });
    }

    private static ItemStack getItemStack(JsonElement json, Consumer<String> onError) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString())
            return BuiltInRegistries.ITEM.get(getResourceLocation(json, onError)).getDefaultInstance();
        if (json instanceof JsonObject object && !object.has("Count"))
            return BuiltInRegistries.ITEM.get(getResourceLocation(object.get("id"), onError)).getDefaultInstance();
        return ItemStack.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(true, onError);
    }

    private static ResourceLocation getResourceLocation(JsonElement json, Consumer<String> onError) {
        return ResourceLocation.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(true, onError);
    }

    public static void registerSerializer(String name, Function<JsonObject, RecipeFilter> deserializer) {
        deserializers.put(name, deserializer);
    }
}
