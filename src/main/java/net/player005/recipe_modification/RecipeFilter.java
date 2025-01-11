package net.player005.recipe_modification;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A simple functional interface to filter recipes.
 * There are some simple filters listed below, but you can easily implement this on your own
 *
 * @see #ALWAYS_APPLY
 * @see #resultItemIs(Item)
 * @see #acceptsIngredient(ItemStack)
 * @see #or(RecipeFilter...)
 * @see #and(RecipeFilter...)
 */
public interface RecipeFilter {

    /**
     * The main (and only) method of a RecipeFilter
     *
     * @param recipe the given recipe to test
     * @return if the test was successful
     */
    boolean shouldApply(RecipeHolder<?> recipe, HolderLookup.Provider registryAccess);

    /**
     * A simple recipe filter that always returns {@code true}.
     */
    RecipeFilter ALWAYS_APPLY = (recipe, registryAccess) -> true;

    /**
     * Returns a recipe filter that filters for recipes that use the given ItemStack as an ingredient
     */
    static RecipeFilter acceptsIngredient(ItemStack item) {
        return (recipe, registryAccess) -> {
            for (var ingredient : recipe.value().getIngredients())
                if (ingredient.test(item)) return true;
            return false;
        };
    }

    /**
     * Returns a recipe filter that filters for recipes that create the given result item.
     */
    static RecipeFilter resultItemIs(Item item) {
        return (recipe, registryAccess) -> recipe.value().getResultItem(registryAccess).is(item);
    }

    /**
     * Returns a recipe filter that filters for recipes that create a result item contained in the given tag.
     */
    static RecipeFilter resultItemIs(TagKey<Item> itemTag) {
        return (recipe, registryAccess) -> recipe.value().getResultItem(registryAccess).is(itemTag);
    }

    /**
     * Returns a recipe filter that filters for the recipe with the given id.
     */
    static RecipeFilter idEquals(ResourceLocation id) {
        return (recipe, registryAccess) -> recipe.id().equals(id);
    }

    /**
     * Returns a recipe filter that filters for recipes in the given namespace.
     */
    static RecipeFilter namespaceEquals(String group) {
        return (recipe, registryAccess) -> recipe.id().getNamespace().equals(group);
    }

    /**
     * Concatenates multiple given filters with a logical and.
     */
    static RecipeFilter and(RecipeFilter... filters) {
        return (recipe, registryAccess) -> {
            for (var filter : filters) if (!filter.shouldApply(recipe, registryAccess)) return false;
            return true;
        };
    }

    /**
     * Concatenates multiple given filters with a logical or.
     */
    static RecipeFilter or(RecipeFilter... filters) {
        return (recipe, registryAccess) -> {
            for (var filter : filters) if (filter.shouldApply(recipe, registryAccess)) return true;
            return false;
        };
    }

    /**
     * Returns a recipe filter that filters for recipes that don't match the given filter (inverts the given filter).
     */
    static RecipeFilter not(RecipeFilter filter) {
        return (recipe, registryAccess) -> !filter.shouldApply(recipe, registryAccess);
    }

    abstract class Serialization {
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
            return idEquals(ResourceLocation.parse(string));
        }

        static {
            registerSerializer("always_apply", (json) -> ALWAYS_APPLY);
            registerSerializer("accepts_ingredient", (json) -> {
                var item = ItemStack.CODEC.parse(JsonOps.INSTANCE, json.get("item")).getOrThrow();
                return acceptsIngredient(item);
            });
            registerSerializer("result_item_is", (json) -> {
                var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.get("item").getAsString()));
                return resultItemIs(item);
            });
            registerSerializer("id_equals", (json) -> {
                var id = ResourceLocation.parse(json.get("id").getAsString());
                return idEquals(id);
            });
            registerSerializer("namespace_equals", (json) -> {
                var namespace = json.get("namespace").getAsString();
                return namespaceEquals(namespace);
            });
        }

        public static void registerSerializer(String name, Function<JsonObject, RecipeFilter> deserializer) {
            deserializers.put(name, deserializer);
        }
    }
}
