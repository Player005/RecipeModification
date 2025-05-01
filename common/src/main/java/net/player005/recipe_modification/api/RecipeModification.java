package net.player005.recipe_modification.api;

import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
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

    public static final String modID = "recipe_modification";
    static final Logger logger = LoggerFactory.getLogger(RecipeModification.class);
    @SuppressWarnings("NotNullFieldNotInitialized")
    private static Platform platform;

    private static final NonNullList<Consumer<RecipeManager>> recipeManagerCallbacks = NonNullList.create();
    private static final Multimap<RecipeFilter, Consumer<RecipeHolder<?>>> recipeIterationCallbacks = ArrayListMultimap.create();

    private static final NonNullList<ResourceLocation> toRemove = NonNullList.create();
    private static final NonNullList<RecipeModifierHolder> modifiers = NonNullList.create();
    private static @UnknownNullability ImmutableList<RecipeModifierHolder> modifiersFromDatapack;
    public static final Multimap<Recipe<?>, ResultItemModifier> resultModifiers = ArrayListMultimap.create();

    private static @UnknownNullability ImmutableMultimap<Item, RecipeHolder<?>> recipesByResult;

    private static @UnknownNullability RecipeManager recipeManager;
    private static HolderLookup.@UnknownNullability Provider registries;

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
     * The given lambda will be called once for EVERY loaded recipe matching the given filter,
     * every time datapacks are reloaded.
     * If this is called before recipe initialization, it will be executed when recipes are loaded.
     * Otherwise, it will be executed immediately (but off-thread).
     *
     * @apiNote The given consumer might be executed asynchronously i.e. not on the main thread.
     */
    public static void forAllRecipes(Consumer<RecipeHolder<?>> recipeConsumer, RecipeFilter filter) {
        if (isInitialised())
            CompletableFuture.runAsync(() -> recipeManager.getRecipes().forEach(recipeConsumer));
        else recipeIterationCallbacks.put(filter, recipeConsumer);
    }

    /**
     * The given lambda will be called once for EVERY loaded recipe, every time datapacks are reloaded.
     * If this is called before recipe initialistion, it will be executed when recipes are loaded.
     * Otherwise, it will be executed immediately (but off-thread).
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
                found.set(recipeHolder.id().location());
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
        toRemove.add(recipeHolder.id().location());
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
        return getPlatform().getRecipeByID(recipeManager, id);
    }

    @ApiStatus.Internal
    public static void initPlatform(Platform platform) {
        RecipeModification.platform = platform;
    }

    /**
     * Returns the current {@link Platform}, an interface for some things that
     * need to be handled differently on different minecraft versions.
     */
    public static Platform getPlatform() {
        return platform;
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
        if (registries == null) throw new IllegalStateException("Registry access not initialised yet");
        return registries;
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
        var fullList = new ArrayList<RecipeModifierHolder>(modifiers.size() + modifiersFromDatapack.size());
        fullList.addAll(modifiers);
        fullList.addAll(modifiersFromDatapack);
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

        if (recipeInput != null && i > 0) logger.debug("Applied {} result item modifiers", i);
        return currentResult;
    }

    @ApiStatus.Internal
    public static void onRecipeManagerLoad(RecipeManager recipeManager) {
        RecipeModification.recipeManager = recipeManager;
        if (modifiersFromDatapack == null)
            throw new IllegalStateException("Recipes were loaded before recipe modifiers from datapacks");
        applyModifications();
    }

    @ApiStatus.Internal
    public static void updateJsonRecipeModifiers(ImmutableList<RecipeModifierHolder> modifiers) {
        modifiersFromDatapack = modifiers;
    }

    @ApiStatus.Internal
    public static void initRegistries(HolderLookup.Provider registries) {
        RecipeModification.registries = registries;
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
            var result = Util.getResultItem(recipeHolder);
            if (result == null) continue;
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

        logger.info("Found {} recipe modifiers in datapacks, {} total",
                modifiersFromDatapack.size(), getAllModifiers().size());

        for (RecipeHolder<?> recipeHolder : recipeManager.getRecipes()) {
            final var registryAccess = getRegistryAccess();

            for (final var entry : recipeIterationCallbacks.entries()) {
                if (entry.getKey().shouldApply(recipeHolder, registryAccess)) entry.getValue().accept(recipeHolder);
            }

            // apply recipeModifiers
            var appliedOnRecipe = 0;
            for (RecipeModifierHolder modifier : getAllModifiers()) {
                if (!modifier.filter().shouldApply(recipeHolder, registryAccess)) continue;
                RecipeHelper helper = getPlatform().getHelper();
                modifier.apply(recipeHolder.value(), helper);
                appliedOnRecipe++;
            }

            if (appliedOnRecipe > 0)
                logger.debug("Applied {} recipe modifiers to {}", appliedOnRecipe, recipeHolder.id());


            for (ResourceLocation id : toRemove) {
                if (recipeHolder.id().location().equals(id)) {
                    // remove recipe from both maps stored in RecipeManager
                    recipeManager.getRecipes().remove(recipeHolder); // TODO: move to platform
                }
            }
            modified += appliedOnRecipe;
        }
        logger.info("Modified {} recipes in {}", modified, timer);
        recipeIterationCallbacks.clear();
    }
}
