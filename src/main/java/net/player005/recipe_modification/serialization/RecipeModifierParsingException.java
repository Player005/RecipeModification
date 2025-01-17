package net.player005.recipe_modification.serialization;

import com.google.gson.JsonParseException;

public class RecipeModifierParsingException extends JsonParseException {
    public RecipeModifierParsingException(String message) {
        super(message);
    }
}
