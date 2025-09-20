package net.player005.recipe_modification.api;

import com.google.common.base.Stopwatch;
import com.google.common.collect.*;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.player005.recipe_modification.impl.mixin.RecipeManagerAccessor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The central class for recipe modifications, containing some utility methods.
 *
 * @see #registerModifier(RecipeModifierHolder)
 * @see #registerModifier(ResourceLocation, RecipeFilter, RecipeModifier...)
 * @see #removeRecipe(ResourceLocation)
 * @see #forAllRecipesAsync(Consumer)
 * @see #onRecipeInit(Consumer)
 */
public abstract class RecipeModification {

    public static final String modID = "recipe_modification";
    private static final Logger logger = LoggerFactory.getLogger(RecipeModification.class);
    @SuppressWarnings("NotNullFieldNotInitialized")
    private static Platform platform;

    private static final NonNullList<Consumer<RecipeManager>> recipeManagerCallbacks = NonNullList.create();

    private static final NonNullList<ResourceLocation> toRemove = NonNullList.create();
    private static final NonNullList<RecipeModifierHolder> modifiers = NonNullList.create();
    private static @UnknownNullability ImmutableList<RecipeModifierHolder> modifiersFromDatapack;
    public static final List<ResultItemModifier> resultModifiers = NonNullList.create();
    private static final Map<Recipe<?>, ItemStack> resultItemOverrides = new IdentityHashMap<>();

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
     * The given lambda will be called once for EVERY loaded recipe, off-thread
     *
     * @apiNote The given consumer might be executed asynchronously
     */
    public static CompletableFuture<Void> forAllRecipesAsync(Consumer<Recipe<?>> recipeConsumer) {
        return CompletableFuture.runAsync(() -> recipeManager.getRecipes().forEach(recipeConsumer));
    }

    /**
     * Registers a {@link ResultItemModifier} to be applied to all recipes.
     *
     * @see RecipeModification#modifyResultItemSimple(Recipe, Consumer)
     * @see RecipeModification#replaceResultItem(Recipe, ItemStack)
     */
    public static void registerGlobalResultModifier(ResultItemModifier modifier) {
        resultModifiers.add(modifier);
    }

    /**
     * Modifies the result item of the given recipe.
     *
     * @see RecipeModification#registerGlobalResultModifier(ResultItemModifier)
     * @see RecipeModification#replaceResultItem(Recipe, ItemStack)
     * @see RecipeModifier#replaceResultItem(Function)
     */
    public static void modifyResultItemSimple(Recipe<?> recipe, Consumer<ItemStack> modifier) {
        var result = recipe.getResultItem(getRegistryAccess());
        modifier.accept(result);
        if (recipe.getResultItem(getRegistryAccess()) == result) return;

        registerGlobalResultModifier((recipe1, result1, recipeInput) -> {
            if (recipe1 == recipe) modifier.accept(result1);
            return result1;
        });
    }

    /**
     * Overrides the result item of the given recipe.
     *
     * @see RecipeModification#modifyResultItemSimple(Recipe, Consumer)
     * @see RecipeModifier#replaceResultItem(ItemStack)
     */
    public static void replaceResultItem(Recipe<?> recipe, ItemStack newResult) {
        resultItemOverrides.put(recipe, newResult);
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

    public static ItemStack tryGetResult(Recipe<?> recipe, RegistryAccess registryAccess) {
        try {
            return recipe.getResultItem(registryAccess);
        } catch (Exception exception) {
            logger.warn("Failed to get result for recipe {}", recipe.getId());
            logger.debug("Exception querying result:", exception);
            return ItemStack.EMPTY;
        }
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
    public static ItemStack getRecipeResult(Recipe<?> recipe, ItemStack currentResult,
                                            @Nullable Container recipeInput) {
        currentResult = resultItemOverrides.getOrDefault(recipe, currentResult).copy();
        for (var modifier : resultModifiers) {
            currentResult = modifier.getResultItem(recipe, currentResult, recipeInput);
        }

        return currentResult;
    }

    @ApiStatus.Internal
    public static void onRecipeManagerLoad(RecipeManager recipeManager) {
        RecipeModification.recipeManager = recipeManager;
        if (modifiersFromDatapack == null)
            throw new IllegalStateException("Error loading Recipe Modification: Recipes were loaded before recipe " +
                "modifiers were parsed");
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

        recipesByResult = buildRecipeResultMap();

        logger.debug("Built recipe by result map for {} recipes in {}", recipeManager.getRecipes().size(), timer);
        timer.reset().start();

        for (Consumer<RecipeManager> recipeManagerCallback : recipeManagerCallbacks) {
            recipeManagerCallback.accept(recipeManager);
        }
        logger.debug("Executed {} recipe manager callbacks in {}", recipeManagerCallbacks.size(), timer);

        timer.reset().start();
        var modified = 0;

        logger.info("Found {} recipe modifiers in datapacks, {} total",
            modifiersFromDatapack.size(), getAllModifiers().size());

        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            final var registryAccess = getRegistryAccess();

            var appliedOnRecipe = applyAllModifiers(recipe, registryAccess);

            if (appliedOnRecipe > 0)
                logger.debug("Applied {} recipe modifiers to {}", appliedOnRecipe, recipe.getId());

            for (ResourceLocation id : toRemove) {
                if (recipe.getId().equals(id)) {
                    platform.removeRecipe(((RecipeManagerAccessor) recipeManager), recipe.getId());
                }
            }
            modified += appliedOnRecipe;
        }

        logger.info("Modified {} recipes in {}", modified, timer);
    }

    private static int applyAllModifiers(Recipe<?> recipe, RegistryAccess registryAccess) {
        var appliedOnRecipe = 0;
        for (RecipeModifierHolder modifier : getAllModifiers()) {
            if (!modifier.filter().shouldApply(recipe, registryAccess)) continue;
            try {
                RecipeHelper helper = getPlatform().getHelper();
                modifier.apply(recipe, helper);
            } catch (Exception e) {
                handleError(recipe, modifier, e);
            }
            appliedOnRecipe++;
        }
        return appliedOnRecipe;
    }

    private static ImmutableMultimap<Item, Recipe<?>> buildRecipeResultMap() {
        var byResultBuilder = ImmutableMultimap.<Item, Recipe<?>>builder();
        for (Recipe<?> recipe : recipeManager.getRecipes()) {
            var result = tryGetResult(recipe, getRegistryAccess());
            byResultBuilder.put(result.getItem(), recipe);
        }

        return byResultBuilder.build();
    }

    private static void handleError(Recipe<?> recipe, RecipeModifierHolder modifier, Exception e) {
        if (platform.isDevelopmentEnvironment()) {
            logger.error("Failed to apply modifier '{}' to recipe '{}'", modifier.id(), recipe.getId(), e);
        } else {
            logger.warn("Failed to apply modifier '{}' to recipe '{}'", modifier.id(), recipe.getId());
            logger.debug("Exception:", e);
        }
    }
}
