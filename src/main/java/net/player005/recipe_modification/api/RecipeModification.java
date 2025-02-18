package net.player005.recipe_modification.api;

import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * The central class for recipe modifications, containing some utility methods.
 *
 * @see #registerModifier(RecipeModifierHolder)
 * @see #registerModifier(ResourceLocation, RecipeFilter, RecipeModifier...)
 * @see #removeRecipe(ResourceLocation)
 * @see #forAllRecipes(Consumer)
 * @see #onRecipeInit(Consumer)
 */
public abstract class RecipeModification {

    public static final String modID = "recipe_modification";
    private static final Logger logger = LoggerFactory.getLogger(RecipeModification.class);
    @SuppressWarnings("NotNullFieldNotInitialized")
    private static Platform platform;

    private static final List<Consumer<RecipeManager>> recipeManagerCallbacks = Lists.newArrayList();
    private static final Multimap<RecipeFilter, Consumer<Recipe<?>>> recipeIterationCallbacks = ArrayListMultimap.create();

    private static final List<ResourceLocation> toRemove = Lists.newArrayList();
    private static final List<RecipeModifierHolder> modifiers = Lists.newArrayList();
    private static @UnknownNullability ImmutableList<RecipeModifierHolder> modifiersFromDatapack;
    public static final Multimap<Recipe<?>, ResultItemModifier> resultModifiers = ArrayListMultimap.create();

    private static @UnknownNullability ImmutableMultimap<Item, Recipe<?>> recipesByResult;

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
     * The given lambda will be called once for EVERY loaded recipe matching the given filter,
     * every time datapacks are reloaded.
     * If this is called before recipe initialization, it will be executed when recipes are loaded.
     * Otherwise, it will be executed immediately (but off-thread).
     *
     * @apiNote The given consumer might be executed asynchronously i.e. not on the main thread.
     */
    public static void forAllRecipes(Consumer<Recipe<?>> recipeConsumer, RecipeFilter filter) {
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
    public static void forAllRecipes(Consumer<Recipe<?>> recipeConsumer) {
        forAllRecipes(recipeConsumer, RecipeFilter.ALWAYS_APPLY);
    }

    /**
     * Registers a {@link ResultItemModifier} to be applied to the result item of the given recipe
     */
    public static void registerRecipeResultModifier(Recipe<?> recipe, ResultItemModifier modifier) {
        resultModifiers.put(recipe, modifier);
        logger.debug("Registered result item modifier for recipe {}, now {} modifiers total", recipe.getId(), resultModifiers.size());
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
    public static Recipe<?> getByID(ResourceLocation id) {
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

    public static RegistryAccess getRegistryAccess() {
        return getPlatform().getRegistryAccess();
    }

    /**
     * Get an (immutable) multimap from the result item to the recipes creating that item.
     * Can only be called after recipe initialisation (i.e. after {@link #onRecipeInit(Consumer)}
     * callbacks were called).
     *
     * @throws IllegalStateException if called before initialisation (see {@link #getRecipeManager()} docs)
     * @see #getRecipesByResult(Item)
     */
    public static ImmutableMultimap<Item, Recipe<?>> getRecipesByResult() {
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
    public static ImmutableCollection<Recipe<?>> getRecipesByResult(Item resultItem) {
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
    public static ItemStack getRecipeResult(Recipe<?> recipe, ItemStack currentResult, @Nullable Container recipeInput) {
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
        if (modifiersFromDatapack == null) {
            logger.error("Recipes loaded before recipe modifiers - recipe modifiers won't be applied");
            return; // TODO: consider hard crash instead off error
        }
        applyModifications();
    }

    @ApiStatus.Internal
    public static void updateJsonRecipeModifiers(ImmutableList<RecipeModifierHolder> modifiers) {
        modifiersFromDatapack = modifiers;
    }

    /**
     * Internal method that should be called on every datapack reload.
     * Initialises all registered {@link RecipeModifierHolder}s, calls all {@link #onRecipeInit(Consumer)}
     * callbacks and removes recipes registered for removal using {@link #removeRecipe(ResourceLocation)}
     */
    @ApiStatus.Internal
    private static void applyModifications() {
        var timer = Stopwatch.createStarted();

        var byResultBuilder = ImmutableMultimap.<Item, Recipe<?>>builder();
        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            var result = tryGetResult(recipe, getRegistryAccess());
            byResultBuilder.put(result.getItem(), recipe);
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

        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            final var registryAccess = getRegistryAccess();

            for (final var entry : recipeIterationCallbacks.entries()) {
                if (entry.getKey().shouldApply(recipe, registryAccess)) entry.getValue().accept(recipe);
            }

            // apply recipeModifiers
            var appliedOnRecipe = 0;
            for (RecipeModifierHolder modifier : getAllModifiers()) {
                if (!modifier.filter().shouldApply(recipe, registryAccess)) continue;
                RecipeHelper helper = getPlatform().getHelper();
                modifier.apply(recipe, helper);
                appliedOnRecipe++;
            }

            if (appliedOnRecipe > 0)
                logger.debug("Applied {} recipe modifiers to {}", appliedOnRecipe, recipe.getId());


            for (ResourceLocation id : toRemove) {
                if (recipe.getId().equals(id)) {
                    platform.removeRecipe(recipe.getId());
                }
            }
            modified += appliedOnRecipe;
        }
        logger.info("Modified {} recipes in {}", modified, timer);
        recipeIterationCallbacks.clear();
    }

    public static ItemStack tryGetResult(Recipe<?> recipe, RegistryAccess registryAccess) {
        try {
            return recipe.getResultItem(registryAccess);
        } catch (Exception exception) {
            logger.warn("Failed to get result for recipe {}", recipe.getId());
            logger.debug("Exception querying result:", exception);
            return ItemStack.EMPTY;
        }
    }
}
