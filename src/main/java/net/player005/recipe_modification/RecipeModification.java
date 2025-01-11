package net.player005.recipe_modification;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.player005.recipe_modification.mixin.RecipeManagerAccessor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The main class for recipe modifications, containing some utility methods.
 *
 * @see #registerModifier(RecipeModifierHolder)
 * @see #removeRecipe(RecipeHolder)
 * @see #forAllRecipes(Consumer)
 */
public abstract class RecipeModification {

    static final String modID = "recipe_modification";
    static final Logger logger = LoggerFactory.getLogger(RecipeModification.class);

    private static final NonNullList<Consumer<RecipeManager>> recipeManagerCallbacks = NonNullList.create();
    private static final NonNullList<Consumer<RecipeHolder<?>>> recipeIterationCallbacks = NonNullList.create();
    private static final Map<RecipeFilter, Consumer<RecipeHolder<?>>> filteredRecipeCallbacks = new HashMap<>();

    private static final NonNullList<ResourceLocation> toRemove = NonNullList.create();
    private static final NonNullList<RecipeModifierHolder> modifiers = NonNullList.create();
    private static @UnknownNullability NonNullList<RecipeModifierHolder> recipeModifiersFromDatapack;

    private static @UnknownNullability ImmutableMultimap<Item, RecipeHolder<?>> recipesByResult;

    private static @UnknownNullability RecipeManager recipeManager;

    /**
     * This method can be used to have some code be executed when the server is starting, right before
     * we apply recipe recipeModifiers. It is also an easy way to access the {@link RecipeManager}.
     * <p>
     * The given consumer will be executed on every datapack reload on dedicated servers,
     * or everytime a singleplayer world is loaded on the Client.
     * </p>
     */
    public static void onRecipeInit(Consumer<RecipeManager> consumer) {
        recipeManagerCallbacks.add(consumer);
    }

    /**
     * The given lambda will be called once for EVERY loaded recipe.
     * Using this method is cheaper than looping through all recipes yourself since
     * this library already iterates all recipes anyway.
     */
    public static void forAllRecipes(Consumer<RecipeHolder<?>> recipeConsumer) {
        recipeIterationCallbacks.add(recipeConsumer);
    }

    /**
     * The given lambda will be called once for every loaded recipe matching the given filter.
     * Using this method is cheaper than looping through all recipes yourself since
     * this library already iterates all recipes anyway.
     */
    public static void forAllRecipes(Consumer<RecipeHolder<?>> recipeConsumer, RecipeFilter filter) {
        filteredRecipeCallbacks.put(filter, recipeConsumer);
    }

    /**
     * Removes the given recipe from the game
     *
     * @param recipeHolder The recipe to remove
     */
    public static void removeRecipe(RecipeHolder<?> recipeHolder) {
        toRemove.add(recipeHolder.id());
    }

    /**
     * Removes the given recipe from the game
     *
     * @param id The ResourceLocation of the recipe to remove
     */
    public static void removeRecipe(ResourceLocation id) {
        toRemove.add(id);
    }

    /**
     * Registers a {@link RecipeModifierHolder} to be applied when loading recipes.
     */
    public static void registerModifier(RecipeModifierHolder recipeModifier) {
        modifiers.add(recipeModifier);
    }

    /**
     * Registers the given recipe modifications to be applied on all recipes matching the given filter.
     *
     * @see #registerModifier(RecipeModifierHolder)
     */
    public static void registerModifier(ResourceLocation id, RecipeFilter filter, ModificationSet modifications) {
        registerModifier(new RecipeModifierHolder(id, filter, modifications));
    }

    /**
     * Registers the given recipe modifications to be applied on all recipes matching the given filter.
     *
     * @see #registerModifier(RecipeModifierHolder)
     */
    public static void registerModifier(ResourceLocation id, RecipeFilter filter, RecipeModifier... modifications) {
        registerModifier(new RecipeModifierHolder(id, filter, modifications));
    }

    /**
     * Returns recipe associated with the given id, or null if no recipe with that id is loaded
     *
     * @throws IllegalStateException if the recipe manager isn't initialised yet (see {@link #getRecipeManager()})
     */
    public static RecipeHolder<?> getByID(ResourceLocation id) {
        checkInitialised("get recipe by ID");
        return ((RecipeManagerAccessor) recipeManager).getByName().get(id);
    }

    /**
     * Returns the Minecraft server's {@link RecipeManager} saved by this class - might be {@code null} in some cases
     * (when the game is not fully initialised yet). <p>Safe to call after the {@link #onRecipeInit(Consumer)} callbacks
     * were called
     */
    public static @UnknownNullability RecipeManager getRecipeManager() {
        return recipeManager;
    }

    /**
     * Returns the {@link RecipeManager}s registry access (a {@link HolderLookup.Provider})
     *
     * @throws IllegalStateException if called before initialisation (see {@link #getRecipeManager()} docs)
     */
    public static HolderLookup.Provider getRegistryAccess() {
        checkInitialised("get the RecipeManager's registry access");
        return ((RecipeManagerAccessor) recipeManager).getRegistries();
    }

    /**
     * Get an (immutable) multimap from the result item to the recipes creating that item.
     * Can only be called after recipe initialisation (i.e. after {@link #onRecipeInit(Consumer)}
     * callbacks were called).
     *
     * @throws IllegalStateException if called before initialisation (see {@link #getRecipeManager()} docs)
     * @see #getRecipesByResult(Item)
     */
    public static ImmutableMultimap<Item, RecipeHolder<?>> getRecipesByResult() {
        checkInitialised("get recipes by result map");
        return recipesByResult;
    }

    /**
     * Returns all recipes that create the given result item.
     * Can only be called after recipe initialisation (i.e. after {@link #onRecipeInit(Consumer)}
     * callbacks were called).
     *
     * @throws IllegalStateException if called before initialisation (see {@link #getRecipeManager()} docs)
     * @see #getRecipesByResult()
     */
    public static ImmutableCollection<RecipeHolder<?>> getRecipesByResult(Item resultItem) {
        checkInitialised("get recipe by result");
        return recipesByResult.get(resultItem);
    }

    public static List<RecipeModifierHolder> getAllModifiers() {
        var fullList = new ArrayList<RecipeModifierHolder>(modifiers.size() + recipeModifiersFromDatapack.size());
        fullList.addAll(modifiers);
        fullList.addAll(recipeModifiersFromDatapack);
        return fullList;
    }

    @ApiStatus.Internal
    static void updateJsonRecipeModifiers(NonNullList<RecipeModifierHolder> modifiers) {
        recipeModifiersFromDatapack = modifiers;
    }

    private static void checkInitialised(String action) {
        if (recipeManager == null)
            throw new IllegalStateException("Can't " + action + " before recipes are initialised." +
                    "Maybe you need to use RecipeManager#onRecipeInit() ?");
    }

    @ApiStatus.Internal
    static void init() {

    }

    /**
     * Internal method that should be called on every datapack reload.
     * Initialises all registered {@link RecipeModifierHolder}s, calls all {@link #onRecipeInit(Consumer)}
     * callbacks and removes recipes registered for removal using {@link #removeRecipe(RecipeHolder)}
     */
    @ApiStatus.Internal
    public static void onRecipeManagerLoad(RecipeManager recipeManager) {
        RecipeModification.recipeManager = recipeManager;
        var timer = Stopwatch.createStarted();

        var byResultBuilder = ImmutableMultimap.<Item, RecipeHolder<?>>builder();
        for (RecipeHolder<?> recipeHolder : recipeManager.getRecipes()) {
            var result = recipeHolder.value().getResultItem(getRegistryAccess());
            byResultBuilder.put(result.getItem(), recipeHolder);
        }

        recipesByResult = byResultBuilder.build();
        logger.debug("Built recipe by result map for {} recipes in {}", recipeManager.getRecipes().size(), timer);
        timer.reset().start();

        for (Consumer<RecipeManager> recipeManagerCallback : recipeManagerCallbacks) {
            recipeManagerCallback.accept(recipeManager);
        }
        logger.debug("Executed {} recipe callbacks in {}", recipeManagerCallbacks.size(), timer);

        timer.reset().start();
        var modified = 0;

        for (RecipeHolder<?> recipeHolder : recipeManager.getRecipes()) {
            final var registryAccess = getRegistryAccess();

            // call registered callbacks
            for (Consumer<RecipeHolder<?>> recipeIterationCallback : recipeIterationCallbacks) {
                recipeIterationCallback.accept(recipeHolder);
            }

            for (final var entry : filteredRecipeCallbacks.entrySet()) {
                if (entry.getKey().shouldApply(recipeHolder, registryAccess)) entry.getValue().accept(recipeHolder);
            }

            // apply recipe recipeModifiers
            for (RecipeModifierHolder modifier : getAllModifiers()) {
                if (!modifier.filter().shouldApply(recipeHolder, registryAccess)) continue;
                var helper = new ModificationHelper(recipeHolder);
                modifier.apply(recipeHolder.value(), helper);
                modified++;
            }

            for (ResourceLocation id : toRemove) {
                if (recipeHolder.id().equals(id)) {
                    // remove recipe from both maps stored in RecipeManager
                    recipeManager.getRecipes().remove(recipeHolder); // remove from RecipeManager#byName
                    recipeManager.getOrderedRecipes().remove(recipeHolder); // remove from RecipeManager#byType
                }
            }
        }
        logger.info("Modified {} recipes in {}", modified, timer);
    }

}
