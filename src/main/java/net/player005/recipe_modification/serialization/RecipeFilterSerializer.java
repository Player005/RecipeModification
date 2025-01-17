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
        return RecipeFilter.idEquals(ResourceLocation.parse(string));
    }

    static {
        registerSerializer("all_recipes", (json) -> RecipeFilter.ALWAYS_APPLY);
        registerSerializer("accepting_ingredient", (json) -> {
            var item = ItemStack.CODEC.parse(JsonOps.INSTANCE, json.get("item")).getOrThrow();
            return RecipeFilter.acceptsIngredient(item);
        });
        registerSerializer("result_item_is", (json) -> {
            var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.get("item").getAsString()));
            return RecipeFilter.resultItemIs(item);
        });
        registerSerializer("id_equals", (json) -> {
            var id = ResourceLocation.parse(json.get("id").getAsString());
            return RecipeFilter.idEquals(id);
        });
        registerSerializer("namespace_equals", (json) -> {
            var namespace = json.get("namespace").getAsString();
            return RecipeFilter.namespaceEquals(namespace);
        });
    }

    public static void registerSerializer(String name, Function<JsonObject, RecipeFilter> deserializer) {
        deserializers.put(name, deserializer);
    }
}
