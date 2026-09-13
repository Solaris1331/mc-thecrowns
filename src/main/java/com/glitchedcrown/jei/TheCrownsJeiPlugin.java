package com.glitchedcrown.jei;

import com.glitchedcrown.GlitchedCrownMod;
import com.glitchedcrown.recipe.CrownBuilderRecipe;
import com.glitchedcrown.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Optional JEI 15.20+ integration. The main mod remains loadable when JEI is absent. */
@JeiPlugin
public final class TheCrownsJeiPlugin implements IModPlugin {
    public static final RecipeType<CrownBuilderRecipe> CROWN_BUILDER =
            RecipeType.create(GlitchedCrownMod.MOD_ID, "crown_builder", CrownBuilderRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(GlitchedCrownMod.MOD_ID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CrownBuilderRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        GlitchedCrownMod.LOGGER.info("JEI: registered Crown Builder recipe category");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        CrownBuilderJeiRecipeSource.Result source = CrownBuilderJeiRecipeSource.collect();
        List<CrownBuilderRecipe> recipes = source.recipes().stream()
                .filter(recipe -> !recipe.getDisplayResult().is(ModItems.UNLEASHED_CROWN.get()))
                .toList();
        registration.addRecipes(CROWN_BUILDER, recipes);
        GlitchedCrownMod.LOGGER.info("JEI: registered {} Crown Builder recipes from {}", recipes.size(), source.source());
        if (recipes.isEmpty()) {
            GlitchedCrownMod.LOGGER.error("JEI: Crown Builder recipe registration produced 0 recipes; this should never happen for the built-in data set");
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(CROWN_BUILDER,
                ModItems.CROWN_BUILDER_T1.get(), ModItems.CROWN_BUILDER_T2.get(),
                ModItems.CROWN_BUILDER_T3.get(), ModItems.CROWN_BUILDER_T4.get());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                new CrownBuilderRecipeTransferHandler(registration.getTransferHelper()), CROWN_BUILDER);
    }
}
