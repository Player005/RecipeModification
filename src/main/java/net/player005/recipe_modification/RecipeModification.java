package net.player005.recipe_modification;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.player005.recipe_modification.mixin.RecipeManagerAccessor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * The central class for recipe modifications, containing some utility methods.
 *
 * @see #registerModifier(RecipeModifierHolder)
 * @see #registerModifier(ResourceLocation, RecipeFilter, RecipeModifier...)
 * @see #removeRecipe(RecipeHolder)
 * @see #forAllRecipes(Consumer)
 * @see #onRecipeInit(Consumer)
 */
public abstract class RecipeModification {

    static final String modID = "recipe_modification";
    static final Logger logger = LoggerFactory.getLogger(RecipeModification.class);

    private static final NonNullList<Consumer<RecipeManager>> recipeManagerCallbacks = NonNullList.create();
    private static final Multimap<RecipeFilter, Consumer<RecipeHolder<?>>> recipeIterationCallbacks = ArrayListMultimap.create();

    private static final NonNullList<ResourceLocation> toRemove = NonNullList.create();
    private static final NonNullList<RecipeModifierHolder> modifiers = NonNullList.create();
    private static @UnknownNullability NonNullList<RecipeModifierHolder> recipeModifiersFromDatapack;
    public static final Multimap<Recipe<?>, ResultItemModifier> resultModifiers = ArrayListMultimap.create();

    private static @UnknownNullability ImmutableMultimap<Item, RecipeHolder<?>> recipesByResult;

    private static @UnknownNullability RecipeManager recipeManager;

    private static boolean hasUpdatedRecipeManager = false;
    private static boolean hasUpdatedModifiers = false;

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
     * The given lambda will be called once for every loaded recipe matching the given filter,
     * every time datapacks are reloaded.
     * Using this method is usually cheaper than looping through all recipes yourself since
     * this library already iterates through all recipes anyway.
     *
     * @apiNote The given consumer might be executed asynchronously i.e. not on the main thread.
     */
    public static void forAllRecipes(Consumer<RecipeHolder<?>> recipeConsumer, RecipeFilter filter) {
        if (isInitialised())
            CompletableFuture.runAsync(() -> recipeManager.getRecipes().forEach(recipeConsumer));
        else recipeIterationCallbacks.put(filter, recipeConsumer);
    }

    /**
     * The given lambda will be called once for every loaded recipe, every time datapacks are reloaded.
     * Using this method is usually cheaper than looping through all recipes yourself since
     * this library already iterates through all recipes anyway, and it is also asynchronous.
     *
     * @apiNote The given consumer might be executed asynchronously i.e. not on the main thread.
     */
    public static void forAllRecipes(Consumer<RecipeHolder<?>> recipeConsumer) {
        forAllRecipes(recipeConsumer, RecipeFilter.ALWAYS_APPLY);
    }

    /**
     * Registers a {@link ResultItemModifier} to be applied to the result item of the given recipe
     */
    public static void registerRecipeResultModifier(Recipe<?> recipe, ResultItemModifier modifier) {
        resultModifiers.put(recipe, modifier);
        logger.debug("Registered result item modifier for recipe {}, now {} modifiers total", findRecipeID(recipe), resultModifiers.size());
    }

    /**
     * Finds the ResourceLocation of the given recipe. Try to avoid this method if possible,
     * as it will iterate through all recipes every time it is called.
     */
    public static ResourceLocation findRecipeID(Recipe<?> recipe) {
        AtomicReference<ResourceLocation> found = new AtomicReference<>();
        forAllRecipes(recipeHolder -> {
            if (recipeHolder.value().equals(recipe)) {
                found.set(recipeHolder.id());
            }
        });
        return found.get();
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

    public static boolean isInitialised() {
        return recipeManager != null;
    }

    private static void checkInitialised(String action) {
        if (!isInitialised())
            throw new IllegalStateException("Can't " + action + " before recipes are initialised." +
                    "Maybe you need to use RecipeModification#onRecipeInit() ?");
    }

    @ApiStatus.Internal
    public static ItemStack getRecipeResult(Recipe<?> recipe, ItemStack currentResult, @Nullable RecipeInput recipeInput) {
        var i = 0;
        for (var entry : resultModifiers.entries()) {
            if (entry.getKey() != recipe) continue;
            currentResult = entry.getValue().getResultItem(recipe, currentResult, recipeInput);
            i++;
        }

        if (i > 0) logger.debug("Applied {} result item modifiers", i);
        return currentResult;
    }

    @ApiStatus.Internal
    public static void onRecipeManagerLoad(RecipeManager recipeManager) {
        RecipeModification.recipeManager = recipeManager;
        hasUpdatedRecipeManager = true;
        checkForUpdate();
    }

    @ApiStatus.Internal
    static void updateJsonRecipeModifiers(NonNullList<RecipeModifierHolder> modifiers) {
        recipeModifiersFromDatapack = modifiers;
        hasUpdatedModifiers = true;
        checkForUpdate();
    }

    private static void checkForUpdate() {
        if (hasUpdatedRecipeManager && hasUpdatedModifiers) {
            CompletableFuture.runAsync(RecipeModification::applyModifications)
                    .whenComplete((result, throwable) -> {
                        hasUpdatedRecipeManager = false;
                        hasUpdatedModifiers = false;
                    });
        }
    }

    /**
     * Internal method that should be called on every datapack reload.
     * Initialises all registered {@link RecipeModifierHolder}s, calls all {@link #onRecipeInit(Consumer)}
     * callbacks and removes recipes registered for removal using {@link #removeRecipe(RecipeHolder)}
     */
    @ApiStatus.Internal
    private static void applyModifications() {
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

        logger.info("Found {} recipe modifiers in datapacks, {} total", recipeModifiersFromDatapack.size(), getAllModifiers().size());

        for (RecipeHolder<?> recipeHolder : recipeManager.getRecipes()) {
            final var registryAccess = getRegistryAccess();

            for (final var entry : recipeIterationCallbacks.entries()) {
                if (entry.getKey().shouldApply(recipeHolder, registryAccess)) entry.getValue().accept(recipeHolder);
            }

            // apply recipeModifiers
            var appliedOnRecipe = 0;
            for (RecipeModifierHolder modifier : getAllModifiers()) {
                if (!modifier.filter().shouldApply(recipeHolder, registryAccess)) continue;
                var helper = new ModificationHelper(recipeHolder);
                modifier.apply(recipeHolder.value(), helper);
                appliedOnRecipe++;
            }

            if (appliedOnRecipe > 0)
                logger.debug("Applied {} recipe modifiers to {}", appliedOnRecipe, recipeHolder.id());


            for (ResourceLocation id : toRemove) {
                if (recipeHolder.id().equals(id)) {
                    // remove recipe from both maps stored in RecipeManager
                    recipeManager.getRecipes().remove(recipeHolder); // remove from RecipeManager#byName
                    recipeManager.getOrderedRecipes().remove(recipeHolder); // remove from RecipeManager#byType
                }
            }
            modified += appliedOnRecipe;
        }
        logger.info("Modified {} recipes in {}", modified, timer);
    }
}
