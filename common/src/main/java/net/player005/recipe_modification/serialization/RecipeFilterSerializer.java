package net.player005.recipe_modification.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.player005.recipe_modification.api.RecipeFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class RecipeFilterSerializer {

    private static final Map<String, Function<JsonObject, RecipeFilter>> deserializers = new HashMap<>();

    @SuppressWarnings("DataFlowIssue")
    private static final ResourceLocation CRAFTING_SHAPED = ResourceLocation.tryParse("crafting_shaped");
    @SuppressWarnings("DataFlowIssue")
    private static final ResourceLocation CRAFTING_SHAPELESS = ResourceLocation.tryParse("crafting_shapeless");

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

        var rl = ResourceLocation.tryParse(string.replace("#", ""));
        if (rl == null)
            throw new RecipeModifierParsingException("Invalid resource location in shorthand recipe filter: " + string);

        if (string.startsWith("#")) return RecipeFilter.resultItemIs(TagKey.create(Registries.ITEM, rl));
        if (BuiltInRegistries.ITEM.containsKey(rl))
            return RecipeFilter.resultItemIs(BuiltInRegistries.ITEM.get(rl));
        return RecipeFilter.idEquals(rl);
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
                throw new RecipeModifierParsingException("Invalid recipe filter: Invalid id: \"" + json.get("id") +
                    "\"");
            return RecipeFilter.idEquals(id);
        });
        registerSerializer("namespace_equals", (json) -> {
            var namespace = json.get("namespace").getAsString();
            return RecipeFilter.namespaceEquals(namespace);
        });
        registerSerializer("not", (json) -> {
            var filter = fromJson(json.get("filter"));
            return RecipeFilter.not(filter);
        });
        registerSerializer("and", (json) -> {
            var filters = new RecipeFilter[json.get("filters").getAsJsonArray().size()];
            for (int i = 0; i < filters.length; i++) {
                filters[i] = fromJson(json.get("filters").getAsJsonArray().get(i));
            }
            return RecipeFilter.and(filters);
        });
        registerSerializer("or", (json) -> {
            var filters = new RecipeFilter[json.get("filters").getAsJsonArray().size()];
            for (int i = 0; i < filters.length; i++) {
                filters[i] = fromJson(json.get("filters").getAsJsonArray().get(i));
            }
            return RecipeFilter.or(filters);
        });
        registerSerializer("is_recipe_type", (json) -> {
            var rl = ResourceLocation.tryParse(json.get("recipe_type").getAsString());
            assert rl != null;
            if (rl.equals(CRAFTING_SHAPED)) return (recipe, registries) -> recipe instanceof ShapedRecipe;
            if (rl.equals(CRAFTING_SHAPELESS)) return (recipe, registries) -> recipe instanceof ShapelessRecipe;

            var type = BuiltInRegistries.RECIPE_TYPE.get(rl);
            if (type == null)
                throw new RecipeModifierParsingException("Unknown recipe type: " + rl);
            return RecipeFilter.isType(type);
        });
        registerSerializer("result_item_predicate", (json) -> {
            var predicate = ItemPredicate.fromJson(json.get("predicate"));
            return RecipeFilter.resultItemMatches(predicate);
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
