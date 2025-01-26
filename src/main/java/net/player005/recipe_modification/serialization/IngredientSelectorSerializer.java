package net.player005.recipe_modification.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.player005.recipe_modification.api.IngredientSelector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class IngredientSelectorSerializer {
    private static final Map<String, Function<JsonObject, IngredientSelector>> deserializers = new HashMap<>();

    public static IngredientSelector fromJson(JsonElement json) {
        if (json instanceof JsonPrimitive primitive) {
            if (primitive.isNumber()) return IngredientSelector.byOrdinals(primitive.getAsInt());
            if (primitive.isString()) return fromString(primitive.getAsString());
        }
        if (json instanceof JsonArray array) {
            var toReturn = new ArrayList<IngredientSelector>();
            for (JsonElement jsonElement : array) {
                toReturn.add(fromJson(jsonElement));
            }
            return IngredientSelector.and(toReturn.toArray(IngredientSelector[]::new));
        }
        if (json instanceof JsonObject object) {
            var deserializer = deserializers.get(object.get("type").getAsString());
            if (deserializer == null) throw new RecipeModifierParsingException("Invalid ingredient selector: " +
                    "unknown selector type " + object.get("type").getAsString());
            return deserializer.apply(object);
        }
        throw new RecipeModifierParsingException("Invalid ingredient selector");
    }

    private static IngredientSelector fromString(String string) {
        if (string.startsWith("#"))
            return IngredientSelector.matchingTag(TagKey.create(Registries.ITEM, ResourceLocation.parse(string.substring(1))));

        var isStrict = string.endsWith("!");
        if (isStrict) string = string.substring(0, string.length() - 1);

        var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(string)).orElseThrow().value();
        if (item == Items.AIR) throw new RecipeModifierParsingException("Invalid item: " + string);
        return isStrict ? IngredientSelector.matchingItem(item) : IngredientSelector.byItem(item);
    }

    static {
        registerSerializer("all", json -> IngredientSelector.ALL_INGREDIENTS);
        registerSerializer("match_item", json -> {
            var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.get("item").getAsString())).orElseThrow().value();
            return IngredientSelector.byItem(item);
        });
        registerSerializer("match_item_exact", json -> {
            var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.get("item").getAsString())).orElseThrow().value();
            return IngredientSelector.matchingItem(item);
        });
        registerSerializer("match_tag", json -> {
            var tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(json.get("tag").getAsString()));
            return IngredientSelector.matchingTag(tag);
        });
        registerSerializer("from_ordinals", json -> {
            var ordinals = new ArrayList<Integer>();
            if (json.get("ordinals") instanceof JsonArray array)
                for (var jsonElement : array)
                    ordinals.add(jsonElement.getAsInt());
            else if (json.get("ordinal") instanceof JsonPrimitive primitive) {
                ordinals.add(primitive.getAsInt());
            } else throw new RecipeModifierParsingException("Invalid ordinal selector");

            return IngredientSelector.byOrdinals(ordinals);
        });
    }

    public static void registerSerializer(String name, Function<JsonObject, IngredientSelector> deserializer) {
        deserializers.put(name, deserializer);
    }
}
