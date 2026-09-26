package com.thecrowns.jei;

import com.thecrowns.menu.CrownBuilderMenu;
import com.thecrowns.recipe.CrownBuilderRecipe;
import com.thecrowns.registry.ModMenus;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import java.util.Optional;

final class CrownBuilderRecipeTransferHandler implements IRecipeTransferHandler<CrownBuilderMenu, CrownBuilderRecipe> {
    private final IRecipeTransferHandlerHelper helper;
    private final IRecipeTransferHandler<CrownBuilderMenu, CrownBuilderRecipe> delegate;

    CrownBuilderRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
        var info = helper.createBasicRecipeTransferInfo(
                CrownBuilderMenu.class,
                ModMenus.CROWN_BUILDER.get(),
                TheCrownsJeiPlugin.CROWN_BUILDER,
                CrownBuilderMenu.INPUT_SLOT_START,
                CrownBuilderMenu.INPUT_SLOT_COUNT,
                CrownBuilderMenu.PLAYER_SLOT_START,
                CrownBuilderMenu.PLAYER_SLOT_COUNT);
        this.delegate = helper.createUnregisteredRecipeTransferHandler(info);
    }

    @Override public Class<? extends CrownBuilderMenu> getContainerClass() { return CrownBuilderMenu.class; }
    @Override public Optional<MenuType<CrownBuilderMenu>> getMenuType() { return Optional.of(ModMenus.CROWN_BUILDER.get()); }
    @Override public RecipeType<CrownBuilderRecipe> getRecipeType() { return TheCrownsJeiPlugin.CROWN_BUILDER; }

    @Override
    public IRecipeTransferError transferRecipe(CrownBuilderMenu container, CrownBuilderRecipe recipe,
                                               IRecipeSlotsView recipeSlots, Player player,
                                               boolean maxTransfer, boolean doTransfer) {
        if (container.getBuilderTier() < recipe.getRequiredTier()) {
            return helper.createUserErrorWithTooltip(Component.translatable(
                    "jei.thecrowns.tier_too_low", CrownBuilderRecipeCategory.roman(recipe.getRequiredTier())));
        }
        return delegate.transferRecipe(container, recipe, recipeSlots, player, maxTransfer, doTransfer);
    }
}
