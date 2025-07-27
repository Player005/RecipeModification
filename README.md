# Recipe Modification

> [!NOTE]
> This mod is still a work-in-progress. Some things might not yet work correctly.
>
> That being said, simple modifications (and some advanced ones) should already work fine and I encourage you to try it out!
> See below on how.

## What is this?

This mod on it's own doesn't do anything, instead, it's a tool for **mod pack creators, data pack creators, other mod developers** or anyone who likes tinkering to use.
It allows you to automatically apply arbitrary modifications any type of recipe at runtime in a simple way, either by using JSON files in a datapack or using the Java API.

### Some examples:

For example, if you wanted to make it so that all button recipes create 8 butons instead of just 1 because you think the button recipe is a scam otherwise, you can easily do that in just a few lines of JSON!

```json
{
  "target_recipes": "#minecraft:buttons",
  "modifiers": [
    {
      "type": "modify_result_item",
      "function": "set_count",
      "count": 8
    }
  ]
}
```
![A screenshot of eight buttons being crafted from one spruce plank](https://github.com/user-attachments/assets/af25b360-8e62-44ce-a4ef-92f8151e8f90)


Other usecases would be, for example, if some mod's recipes are not properly compatible because they don't utilise tags correctly
(or there are no standardised tags for the relevant items), you can easily make them compatible
(without having to manually override every single recipe! yay!).

Say for example, a mod named "mod_xyz" doesn't use the tag for copper nuggets in their recipes, which makes these incompatible with copper nuggets from other mods.
Using Recipe Modification, you can fix it like this:

```json
{
  "target_recipes": {
    "type": "namespace_equals",
    "namespace": "mod_xyz"
  },
  "modifiers": [
    {
      "type": "add_alternative",
      "ingredients": "mod_xyz:copper_nugget",
      "alternative": {
        "tag": "c:copper_nuggets"
      }
    }
  ]
}
```

## How can I use this?

Generally, the preferred way to interact with this mod is via JSON files.

**Check out [the document about JSON Syntax](https://github.com/Player005/RecipeModification/blob/1.21.1/main/docs/JSON_Syntax.md) for more information on how to write these JSON files and what you can do with this mod.**
There are also some (nonsensical, but working) [test modifiers](https://github.com/Player005/RecipeModification/tree/1.21.1/main/common/src/main/resources/data/testing/recipe_modifier) that you can take inspiration from.

More detailed instructions & usage information coming soon™.
