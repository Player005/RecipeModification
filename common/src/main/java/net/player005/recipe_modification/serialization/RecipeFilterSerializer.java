package net.player005.recipe_modification.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.player005.recipe_modification.api.RecipeFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("SuspiciousToArrayCall")
public abstract class RecipeFilterSerializer {

    private static final Map<String, Function<JsonObject, RecipeFilter>> deserializers = new HashMap<>();

    private static final Identifier CRAFTING_SHAPED = Identifier.parse("crafting_shaped");
    private static final Identifier CRAFTING_SHAPELESS = Identifier.parse("crafting_shapeless");

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
        if (string.startsWith("#")) return RecipeFilter.resultItemIs(
            TagKey.create(Registries.ITEM, Identifier.parse(string.replaceFirst("#", ""))));
        if (string.equals("*")) return RecipeFilter.ALWAYS_APPLY;
        if (!string.contains(":")) return RecipeFilter.namespaceEquals(string);
        var rl = Identifier.tryParse(string);
        if (rl == null)
            throw new RecipeModifierParsingException("Invalid resource location in shorthand recipe filter: " + string);
        if (BuiltInRegistries.ITEM.containsKey(rl))
            return RecipeFilter.resultItemIs(BuiltInRegistries.ITEM.get(rl).orElseThrow().value());
        return RecipeFilter.idEquals(Identifier.parse(string));
    }

    static {
        registerSerializer("all_recipes", (json) -> RecipeFilter.ALWAYS_APPLY);
        registerSerializer("accepting_ingredient", (json) -> {
            var item = ItemStack.CODEC.parse(JsonOps.INSTANCE, json.get("item")).getOrThrow();
            return RecipeFilter.acceptsIngredient(item);
        });
        registerSerializer("result_item_is", json -> createFilterByResultItem(json.get("item")));
        registerSerializer("result_item_predicate", (json) -> {
            RecipeFilter itemFilter = null;
            json = json.get("predicate").getAsJsonObject();

            if (json.has("items")) {
                itemFilter = createFilterByResultItem(json.remove("items"));
            }

            var predicate = ItemPredicate.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
            return itemFilter == null ?
                RecipeFilter.resultItemMatches(predicate) :
                RecipeFilter.and(itemFilter, RecipeFilter.resultItemMatches(predicate));
        });
        registerSerializer("id_equals", (json) -> {
            var id = Identifier.parse(json.get("id").getAsString());
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
        registerSerializer("is_recipe_type", (json) -> {
            var rl = Identifier.parse(json.get("recipe_type").getAsString());
            if (rl.equals(CRAFTING_SHAPED)) return (recipe, registries) -> recipe.value() instanceof ShapedRecipe;
            if (rl.equals(CRAFTING_SHAPELESS)) return (recipe, registries) -> recipe.value() instanceof ShapelessRecipe;
            var type = BuiltInRegistries.RECIPE_TYPE.getValue(rl);
            if (type == null)
                throw new RecipeModifierParsingException("Unknown recipe type: " + json.get("recipe_type").getAsString());
            return RecipeFilter.isType(type);
        });
    }

    private static RecipeFilter createFilterByResultItem(JsonElement json) {
        if (json instanceof JsonArray array) {
            Item[] items = array.asList().stream().map(jsonElement ->
                BuiltInRegistries.ITEM.get(Identifier.parse(jsonElement.getAsString()))).toArray(Item[]::new);
            return RecipeFilter.resultItemIs(items);
        }

        if (!(json instanceof JsonPrimitive primitive && primitive.isString()))
            throw new RecipeModifierParsingException("invalid result item recipe filter: must be either string or " +
                "array of strings: " + json);

        var str = json.getAsString();
        if (str.startsWith("#")) {
            TagKey<Item> itemTag = TagKey.create(Registries.ITEM, Identifier.parse(str.replace("#", "")));
            return RecipeFilter.resultItemIs(itemTag);
        }

        var item = BuiltInRegistries.ITEM.get(Identifier.parse(str));
        return RecipeFilter.resultItemIs(item.orElseThrow().value());
    }

    public static void registerSerializer(String name, Function<JsonObject, RecipeFilter> deserializer) {
        deserializers.put(name, deserializer);
    }
}
