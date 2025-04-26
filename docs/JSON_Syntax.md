# Defining Recipe Modifiers

## Contents

- [General Syntax](#general-syntax)
- [Recipe Filter Syntax](#recipe-filter-syntax)
    - [short-hand-syntax](#short-hand-syntax)
- [Recipe Modifier Syntax](#recipe-modifier-syntax)
- [Ingredient Selector Syntax](#ingredient-selector-syntax)
    - [short-hand-syntax](#short-hand-syntax-1)

## General Syntax

Recipe Modifiers can be defined in a datapack as json files in the
`recipe_modifiers` namespace, for example:
`data/my_mod/recipe_modifiers/my_recipe_modifier.json`

The structure of them looks like this:

```json5
{
  "target_recipes": {
    // recipe filter definition, for example:
    "type": "all_ingredients"
  },
  "modifiers": [
    // list of recipe modifiers
  ]
}
```

Each recipe modifier must define which recipes it should target
(a recipe filter, defined under "target_recipes"), and a list of
modifications that will be applied to the matching recipes.

## Recipe Filter Syntax

A recipe filter takes the list of all available recipes and
selects one or more recipes from them.

There are currently Five different types of recipe filters:
`all_recipes`, `accepting_ingredient`, `result_item_is`, `id_equals`
and `namespace_equals`.
Recipe filters can also be chained using `and` and `or` and inverted
using `not`.

Mods can also define custom recipe filters in java code.

### "all_recipes"

This filter matches all recipes.
It does not take any parameters.

Example:

```json5
{
  "type": "all_recipes"
}
```

### "accepting_ingredient"

This filter takes one item as a parameter and matches all recipes
that accept the given item as an ingredient.

Example:

```json5
{
  "type": "accepting_ingredient",
  "item": {
    "id": "minecraft:stone"
  }
}
```

This would match all recipes that take stone as an input.

### "result_item_is"

This filter takes one item as a parameter and matches all recipes
that can be used to create that item.

Example:

```json5
{
  "type": "result_item_is",
  "item": "minecraft:stone"
}
```

This would match all recipes that create stone (i.e. smelting
cobblestone in a furnace).

### "id_equals"

This filter takes one resource location as a parameter and matches
the recipe with that id.

Example:

```json5
{
  "type": "id_equals",
  "id": "minecraft:gold_ingot_from_gold_block"
}
```

This would only match the recipe that gives you gold ingots from a
gold block.

### "namespace_equals"

This filter takes one string as a parameter and matches all
recipes with that namespace.

Example:

```json5
{
  "type": "namespace_equals",
  "namespace": "create"
}
```

This would match all recipes from the Create mod.

### "and" and "or"

These filters take a list of recipe filters as parameters and
chain them together.

Example:

```json5
{
  "type": "and",
  "filters": [
    {
      "type": "namespace_equals",
      "namespace": "minecraft"
    },
    {
      "type": "accepting_ingredient",
      "item": {
        "id": "minecraft:stone"
      }
    }
  ]
}
```

This would match all recipes that take stone as an input that are
added by minecraft.

### "not"

This filter takes one recipe filter as a parameter and inverts it.

Example:

```json5
{
  "type": "not",
  "filter": {
    "type": "accepting_ingredient",
    "item": {
      "id": "minecraft:stone"
    }
  }
}
```

This would match all recipes that do not take stone as an input.

### Short-Hand Syntax

Instead of using the full recipe filter syntax as shown above,
you can also use the shorthand syntax, which consists of a
single string.

To target all recipes, use `*`.

To target all recipes that create a certain item, just use the
item's id.

To target a specific single recipe, use the recipe's id (note: if
the recipe id equals the id of an item, it will prioritise
matching all recipes that create that item instead).

To target all recipes in a certain namespace, use the namespace

To invert a shorthand filter, put `!` before the filter.

Examples:

```json5
{
  "filter": "minecraft:stone"
}
```

This would match all recipes that create stone (e.g. smelting
cobblestone in a furnace).

```json5
{
  "filter": "!create"
}
```

This would match all recipes that are not from the Create mod.

## Recipe Modifier Syntax

A recipe modifier is what actually applies the modifications to
recipes.

There are currently six types of recipe modifiers:
`add_ingredient`, `remove_ingredient`, `replace_ingredient`,
`add_alternative`, `replace_result_item` and
`modify_result_components`.

Mods can also define custom recipe modifiers in java code.

### "add_ingredient"

This modifier tries to add a given item as an additional required
ingredient to the recipe.

Note that this does not work with all kinds of recipes
(i.e. furnace recipes only allow for exactly 1 ingredient).

Example:

```json5
{
  "type": "add_ingredient",
  "ingredient": {
    "item": "minecraft:stone"
  }
}
```

### "remove_ingredient"

This modifier tries to remove selected ingredients from the recipe.

Note that this does not work with all kinds of recipes.

Example:

```json5
{
  "type": "remove_ingredient",
  "ingredients": "minecraft:stone" // ingredient selector
}
```

This would remove all stone ingredients from the recipe.

### "replace_ingredient"

This modifier replaces selected ingredients with a new one.

Example:

```json5
{
  "type": "replace_ingredient",
  "ingredient": "minecraft:stone", // ingredient selector
  "new_ingredient": {
    "item": "minecraft:dirt"
  }
}
```

This would replace all stone ingredients with dirt (i.e. now you 
need to use dirt instead of stone to craft the item).

### "add_alternative"

This modifier adds an alternative to selected ingredients from
the recipe (i.e. either the original or the new ingredient can be
used).

Example:

```json5
{
  "type": "add_alternative",
  "ingredient": "minecraft:stone", // ingredient selector
  "alternative": {
    "item": "minecraft:dirt"
  }
}
```

This would add dirt as an alternative to stone for selected
recipes, i.e. now either dirt or stone could be used to
craft the item.

### "replace_result_item"

This modifier replaces the result of the recipe with a new one.

Example:

```json5
{
  "type": "replace_result_item",
  "new_result": {
    "Count": 1,
    "id": "minecraft:dirt"
  }
}
```

This would replace the result of the recipe with dirt.

## Ingredient Selector Syntax

As you saw earlier, some recipe modifiers take an ingredient
selector as a parameter
(I marked them with `// ingredient selector`).

These can either use the shorthand string syntax that you saw
or a full object syntax, with five different types of selectors:
`all`, `match_item`, `match_item_exact`, `match_tag` and
`from_ordinals`.

### "all"

This selector selects all ingredients of the recipe.

### "match_item"

This selector selects all ingredients that match the given item.
This includes tag ingredients that contain the item.

Example:

```json5
{
  "type": "match_item",
  "item": {
    "id": "minecraft:stone"
  }
}
```

### "match_item_exact"

This selector selects all ingredients that ***only*** match the
given item, i.e. it will not match tag ingredients that contain
the item or ingredients with multiple alternative items.

Example:

```json5
{
  "type": "match_item_exact",
  "item": "minecraft:stone"
}
```

### "match_tag"

This selector selects all ingredients that match the given tag.

Example:

```json5
{
  "type": "match_tag",
  "tag": "minecraft:planks"
}
```

### "from_ordinals"

This selector selects all ingredients by their id/ordinal.
It takes either a single int or a list of integers as a parameter.

Example:

```json5
{
  "type": "from_ordinals",
  "ordinals": 0
}
```

This would select the first ingredient of the recipe.

### Short-hand Syntax

Again, there is a shorthand string syntax that you can use to
define these selectors.

To target all ingredients, use `*`.

To match a certain item, just use the item id.

For `match_item_exact` (i.e. excluding ingredients that allow for
multiple alternative items), add a `!` after the item id.

To match tag ingredients, use `#` followed by the tag name.

To match an ingredient by its id/ordinal, just use the ordinal.

Examples:

```json5
{
  "ingredient": 0
}
```

This would select the first ingredient of the recipe.

```json5
{
  "ingredient": "*"
}
```

This would select all ingredients of the recipe.

```json5
{
  "ingredient": "minecraft:stone"
}
```

This would select all stone ingredients of the recipe.
