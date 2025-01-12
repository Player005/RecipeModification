package net.player005.recipe_modification;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.function.Function;

/**
 * An object that selects ingredients based on certain criteria when given a recipe.
 * Can easily be used either as a functional interface, or by using the static methods provided by this class.
 */
@FunctionalInterface
public interface IngredientSelector {

    Ingredient[] selectIngredients(Recipe<?> recipe);

    /**
     * Always select all ingredients of the given recipe.
     */
    IngredientSelector ALL_INGREDIENTS = recipe -> recipe.getIngredients().toArray(Ingredient[]::new);

    /**
     * Selects ingredients by their id/ordinal.
     */
    static IngredientSelector byOrdinals(int... numbers) {
        return recipe -> Arrays.stream(numbers).mapToObj(recipe.getIngredients()::get).toArray(Ingredient[]::new);
    }

    /**
     * Selects ingredients by their id/ordinal.
     */
    static IngredientSelector byOrdinals(List<Integer> numbers) {
        return recipe -> numbers.stream().map(recipe.getIngredients()::get).toArray(Ingredient[]::new);
    }

    /**
     * Selects all ingredients that contain the given item (including tag ingredients that contain the item).
     *
     * @see #matchingItem(Item)
     */
    static IngredientSelector byItem(Item item) {
        return recipe -> {
            var toReturn = new ArrayList<Ingredient>();
            for (var ingredient : recipe.getIngredients()) {
                if (ArrayUtils.contains(ingredient.getItems(), item)) toReturn.add(ingredient);
            }
            return toReturn.toArray(Ingredient[]::new);
        };
    }

    /**
     * Selects all ingredients that match exactly the given item (not including tag ingredients that contain the item).
     *
     * @see #byItem(Item)
     */
    static IngredientSelector matchingItem(Item item) {
        return recipe -> {
            var toReturn = new ArrayList<Ingredient>();
            for (var ingredient : recipe.getIngredients()) {
                for (var value : ((IngredientAccessor) (Object) ingredient).getValues()) {
                    if (value instanceof Ingredient.ItemValue(ItemStack ingredientStack) && ingredientStack.is(item))
                        toReturn.add(ingredient);
                }
            }
            return toReturn.toArray(Ingredient[]::new);
        };
    }

    /**
     * Selects all ingredients that match the given tag.
     */
    static IngredientSelector matchingTag(TagKey<Item> tag) {
        return recipe -> {
            var toReturn = new ArrayList<Ingredient>();
            for (var ingredient : recipe.getIngredients()) {
                for (var value : ((IngredientAccessor) (Object) ingredient).getValues()) {
                    if (value instanceof Ingredient.TagValue(TagKey<Item> ingredientTag) && ingredientTag.equals(tag))
                        toReturn.add(ingredient);
                }
            }
            return toReturn.toArray(Ingredient[]::new);
        };
    }

    /**
     * Selects all ingredients that match any of the given selectors.
     */
    static IngredientSelector and(IngredientSelector... selectors) {
        return recipe -> {
            var toReturn = new ArrayList<Ingredient>();
            for (var selector : selectors) {
                toReturn.addAll(Arrays.asList(selector.selectIngredients(recipe)));
            }
            return toReturn.toArray(Ingredient[]::new);
        };
    }


    abstract class Serialization {
        private static final Map<String, Function<JsonObject, IngredientSelector>> deserializers = new HashMap<>();

        public static IngredientSelector fromJson(JsonElement json) {
            if (json instanceof JsonPrimitive primitive) {
                if (primitive.isNumber()) return byOrdinals(primitive.getAsInt());
                if (primitive.isString()) return fromString(primitive.getAsString());
            }
            if (json instanceof JsonArray array) {
                var toReturn = new ArrayList<IngredientSelector>();
                for (JsonElement jsonElement : array) {
                    toReturn.add(fromJson(jsonElement));
                }
                return and(toReturn.toArray(IngredientSelector[]::new));
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
                return matchingTag(TagKey.create(Registries.ITEM, ResourceLocation.parse(string.substring(1))));

            var isStrict = string.endsWith("!");
            if (isStrict) string = string.substring(0, string.length() - 1);

            var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(string));
            if (item == Items.AIR) throw new RecipeModifierParsingException("Invalid item: " + string);
            return isStrict ? matchingItem(item) : byItem(item);
        }

        static {
            registerSerializer("all", json -> ALL_INGREDIENTS);
            registerSerializer("match_item", json -> {
                var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.get("item").getAsString()));
                return byItem(item);
            });
            registerSerializer("match_item_exact", json -> {
                var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.get("item").getAsString()));
                return matchingItem(item);
            });
            registerSerializer("match_tag", json -> {
                var tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(json.get("tag").getAsString()));
                return matchingTag(tag);
            });
            registerSerializer("from_ordinals", json -> {
                var ordinals = new ArrayList<Integer>();
                if (json.get("ordinals") instanceof JsonArray array)
                    for (var jsonElement : array)
                        ordinals.add(jsonElement.getAsInt());
                else if (json.get("ordinal") instanceof JsonPrimitive primitive) {
                    ordinals.add(primitive.getAsInt());
                } else throw new RecipeModifierParsingException("Invalid ordinal selector");

                return byOrdinals(ordinals);
            });
        }

        public static void registerSerializer(String name, Function<JsonObject, IngredientSelector> deserializer) {
            deserializers.put(name, deserializer);
        }
    }
}
