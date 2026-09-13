package com.thecrowns.jei;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.recipe.CrownBuilderRecipe;
import com.thecrowns.registry.ModRecipes;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gets Crown Builder recipes for JEI without depending on Minecraft.level being non-null.
 * JEI starts after recipe synchronization, when the ClientPacketListener normally already owns
 * the authoritative client RecipeManager. A classpath fallback keeps the built-in recipes
 * visible even in unusual startup ordering or menu-only test environments.
 */
final class CrownBuilderJeiRecipeSource {
    private static final String[] BUILTIN_IDS = {
            "burning_crown", "ironforged_crown", "frost_crown",
            "bloody_crown", "darkened_crown", "warrior_crown", "divine_crown",
            "crown_of_light", "dimensional_crown", "angelic_crown", "temporal_crown",
            "glitched_crown", "unleashed_crown"
    };

    private CrownBuilderJeiRecipeSource() {}

    static Result collect() {
        RecipeManager manager = findClientRecipeManager();
        if (manager != null) {
            List<CrownBuilderRecipe> synced = manager.getAllRecipesFor(ModRecipes.CROWN_BUILDER_TYPE.get());
            if (!synced.isEmpty()) {
                return new Result(sortedUnique(synced), "client RecipeManager");
            }
            TheCrownsMod.LOGGER.warn("JEI: client RecipeManager contains no Crown Builder recipes; using built-in JSON fallback");
        } else {
            TheCrownsMod.LOGGER.warn("JEI: client RecipeManager is not available during recipe registration; using built-in JSON fallback");
        }

        return new Result(loadBuiltins(), "built-in JSON fallback");
    }

    private static RecipeManager findClientRecipeManager() {
        Minecraft minecraft = Minecraft.getInstance();
        var connection = minecraft.getConnection();
        if (connection != null) return connection.getRecipeManager();
        if (minecraft.level != null) return minecraft.level.getRecipeManager();
        return null;
    }

    private static List<CrownBuilderRecipe> loadBuiltins() {
        List<CrownBuilderRecipe> recipes = new ArrayList<>(BUILTIN_IDS.length);
        for (String path : BUILTIN_IDS) {
            ResourceLocation id = new ResourceLocation(TheCrownsMod.MOD_ID, path);
            String resourcePath = "/data/" + TheCrownsMod.MOD_ID + "/recipes/" + path + ".json";
            try (InputStream stream = CrownBuilderJeiRecipeSource.class.getResourceAsStream(resourcePath)) {
                if (stream == null) {
                    TheCrownsMod.LOGGER.error("JEI: missing built-in Crown Builder recipe resource {}", resourcePath);
                    continue;
                }
                JsonObject json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                CrownBuilderRecipe recipe = ModRecipes.CROWN_BUILDER.get().fromJson(id, json);
                if (recipe != null) recipes.add(recipe);
            } catch (Exception ex) {
                TheCrownsMod.LOGGER.error("JEI: failed to load built-in Crown Builder recipe {}", id, ex);
            }
        }
        return sortedUnique(recipes);
    }

    private static List<CrownBuilderRecipe> sortedUnique(List<CrownBuilderRecipe> recipes) {
        Map<ResourceLocation, CrownBuilderRecipe> byId = new LinkedHashMap<>();
        recipes.stream()
                .sorted(Comparator.comparing(r -> r.getId().toString()))
                .forEach(recipe -> byId.put(recipe.getId(), recipe));
        return List.copyOf(byId.values());
    }

    record Result(List<CrownBuilderRecipe> recipes, String source) {}
}
