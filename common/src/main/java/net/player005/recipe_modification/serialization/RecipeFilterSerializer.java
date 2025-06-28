package net.player005.recipe_modification.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.player005.recipe_modification.api.RecipeFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("SuspiciousToArrayCall")
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
        if (string.startsWith("!")) return RecipeFilter.not(fromString(string.substring(1)));
        if (string.equals("*")) return RecipeFilter.ALWAYS_APPLY;
        if (!string.contains(":")) return RecipeFilter.namespaceEquals(string);
        var rl = ResourceLocation.tryParse(string);
        if (rl == null)
            throw new RecipeModifierParsingException("Invalid resource location in shorthand recipe filter: " + string);
        if (BuiltInRegistries.ITEM.containsKey(rl))
            //noinspection OptionalGetWithoutIsPresent
            return RecipeFilter.resultItemIs(BuiltInRegistries.ITEM.get(rl).get().value());
        return RecipeFilter.idEquals(ResourceLocation.parse(string));
    }

    static {
        registerSerializer("all_recipes", (json) -> RecipeFilter.ALWAYS_APPLY);
        registerSerializer("accepting_ingredient", (json) -> {
            var item = ItemStack.CODEC.parse(JsonOps.INSTANCE, json.get("item")).getOrThrow();
            return RecipeFilter.acceptsIngredient(item);
        });
        registerSerializer("result_item_is", (json) -> {
            var item =
                BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.get("item").getAsString())).orElseThrow().value();
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
        registerSerializer("and", (json) -> {
            var jsonFilters = json.getAsJsonArray("filters");
            var filters = new ArrayList<>();
            for (var filter : jsonFilters)
                filters.add(fromJson(filter));
            return RecipeFilter.and(filters.toArray(RecipeFilter[]::new));
        });
        registerSerializer("or", (json) -> {
            var jsonFilters = json.getAsJsonArray("filters");
            var filters = new ArrayList<>();
            for (var filter : jsonFilters)
                filters.add(fromJson(filter));
            return RecipeFilter.or(filters.toArray(RecipeFilter[]::new));
        });
        registerSerializer("not", (json) -> {
            var filter = fromJson(json.get("filter"));
            return RecipeFilter.not(filter);
        });
        registerSerializer("is_type", (json) -> {
            var type = BuiltInRegistries.RECIPE_TYPE.getValue(ResourceLocation.parse(json.get("type").getAsString()));
            if (type == null)
                throw new RecipeModifierParsingException("Unknown recipe type: " + json.get("type").getAsString());
            return RecipeFilter.isType(type);
        });
    }

    public static void registerSerializer(String name, Function<JsonObject, RecipeFilter> deserializer) {
        deserializers.put(name, deserializer);
    }
}
