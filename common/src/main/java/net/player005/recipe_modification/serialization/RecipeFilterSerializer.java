package net.player005.recipe_modification.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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

    private static final ResourceLocation CRAFTING_SHAPED = ResourceLocation.parse("crafting_shaped");
    private static final ResourceLocation CRAFTING_SHAPELESS = ResourceLocation.parse("crafting_shapeless");

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
            return RecipeFilter.resultItemIs(BuiltInRegistries.ITEM.get(rl));
        return RecipeFilter.idEquals(ResourceLocation.parse(string));
    }

    static {
        registerSerializer("all_recipes", (json) -> RecipeFilter.ALWAYS_APPLY);
        registerSerializer("accepting_ingredient", (json) -> {
            var item = ItemStack.CODEC.parse(JsonOps.INSTANCE, json.get("item")).getOrThrow();
            return RecipeFilter.acceptsIngredient(item);
        });
        registerSerializer("result_item_is", json -> {
            if (json.has("items"))
                return createFilterByResultItem(json.get("items"));
            if (json.has("item"))
                return createFilterByResultItem(json.get("item"));
            throw new RecipeModifierParsingException("'result_item_is' filter has no 'items' or 'item' defined - " +
                "typo?");
        });
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
        registerSerializer("is_recipe_type", (json) -> {
            var rl = ResourceLocation.parse(json.get("recipe_type").getAsString());

            if (rl.equals(CRAFTING_SHAPED)) return (recipe, registries) -> recipe.value() instanceof ShapedRecipe;
            if (rl.equals(CRAFTING_SHAPELESS)) return (recipe, registries) -> recipe.value() instanceof ShapelessRecipe;

            var type = BuiltInRegistries.RECIPE_TYPE.get(rl);
            if (type == null)
                throw new RecipeModifierParsingException("Unknown recipe type: " + rl);
            return RecipeFilter.isType(type);
        });
    }

    private static RecipeFilter createFilterByResultItem(JsonElement json) {
        if (json instanceof JsonArray array) {
            Item[] items = array.asList().stream().map(jsonElement ->
                BuiltInRegistries.ITEM.get(ResourceLocation.parse(jsonElement.getAsString()))).toArray(Item[]::new);
            return RecipeFilter.resultItemIs(items);
        }

        if (!(json instanceof JsonPrimitive primitive && primitive.isString()))
            throw new RecipeModifierParsingException("invalid result item recipe filter: must be either string or " +
                "array of strings: " + json);

        var str = json.getAsString();
        if (str.startsWith("#")) {
            TagKey<Item> itemTag = TagKey.create(Registries.ITEM, ResourceLocation.parse(str.replace("#", "")));
            return RecipeFilter.resultItemIs(itemTag);
        }

        var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(str));
        return RecipeFilter.resultItemIs(item);
    }

    public static void registerSerializer(String name, Function<JsonObject, RecipeFilter> deserializer) {
        deserializers.put(name, deserializer);
    }
}
