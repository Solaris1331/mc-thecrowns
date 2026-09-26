package com.thecrowns.rei;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.menu.CrownBuilderMenu;
import com.thecrowns.recipe.CrownBuilderRecipe;
import com.thecrowns.registry.ModItems;
import com.thecrowns.registry.ModRecipes;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.forge.REIPluginClient;

/** Native REI 12 integration. Loaded by REI only when REI is installed. */
@REIPluginClient
public final class TheCrownsReiPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<CrownBuilderReiDisplay> CROWN_BUILDER =
            CategoryIdentifier.of(TheCrownsMod.MOD_ID, "crown_builder");

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new CrownBuilderReiCategory());
        registry.addWorkstations(CROWN_BUILDER,
                EntryIngredients.of(ModItems.CROWN_BUILDER_T1.get()),
                EntryIngredients.of(ModItems.CROWN_BUILDER_T2.get()),
                EntryIngredients.of(ModItems.CROWN_BUILDER_T3.get()),
                EntryIngredients.of(ModItems.CROWN_BUILDER_T4.get()));
        TheCrownsMod.LOGGER.info("REI: registered Crown Builder category and 4 workstations");
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipeFiller(CrownBuilderRecipe.class, ModRecipes.CROWN_BUILDER_TYPE.get(),
                recipe -> recipe.isRecipeEnabled() && !recipe.getDisplayResult().is(ModItems.UNLEASHED_CROWN.get())
                        ? new CrownBuilderReiDisplay(recipe) : null);
        TheCrownsMod.LOGGER.info("REI: registered Crown Builder display filler");
    }

    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(SimpleTransferHandler.create(
                CrownBuilderMenu.class,
                CROWN_BUILDER,
                new SimpleTransferHandler.IntRange(CrownBuilderMenu.INPUT_SLOT_START,
                        CrownBuilderMenu.INPUT_SLOT_START + CrownBuilderMenu.INPUT_SLOT_COUNT),
                new SimpleTransferHandler.IntRange(CrownBuilderMenu.PLAYER_SLOT_START,
                        CrownBuilderMenu.PLAYER_SLOT_START + CrownBuilderMenu.PLAYER_SLOT_COUNT)));
        TheCrownsMod.LOGGER.info("REI: registered Crown Builder transfer handler");
    }
}
