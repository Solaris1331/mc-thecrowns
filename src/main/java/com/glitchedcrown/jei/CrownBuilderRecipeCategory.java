package com.glitchedcrown.jei;

import com.glitchedcrown.recipe.CrownBuilderRecipe;
import com.glitchedcrown.registry.ModItems;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;

public final class CrownBuilderRecipeCategory implements IRecipeCategory<CrownBuilderRecipe> {
    private static final int WIDTH = 128;
    private static final int HEIGHT = 78;

    private final IDrawable icon;

    public CrownBuilderRecipeCategory(IGuiHelper guiHelper) {
        // JEI 15.20+ allows categories to define their dimensions without a background drawable.
        // This deliberately avoids the old createBlankDrawable ABI pitfall.
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.CROWN_BUILDER_T1.get()));
    }

    @Override public RecipeType<CrownBuilderRecipe> getRecipeType() { return TheCrownsJeiPlugin.CROWN_BUILDER; }
    @Override public Component getTitle() { return Component.translatable("jei.glitchedcrown.crown_builder.title"); }
    @Override public int getWidth() { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CrownBuilderRecipe recipe, IFocusGroup focuses) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slot = row * 3 + col;
                IRecipeSlotBuilder jeiSlot = builder.addInputSlot(1 + col * 18, 1 + row * 18)
                        .setStandardSlotBackground();
                IIngredientAcceptor<?> ingredientSlot = jeiSlot;

                ItemStack special = recipe.getSpecialDisplayStack(slot);
                if (!special.isEmpty()) {
                    ingredientSlot.addItemStack(special);
                } else {
                    Ingredient ingredient = recipe.getIngredients().get(slot);
                    ingredientSlot.addIngredients(ingredient);
                }
            }
        }

        IRecipeSlotBuilder outputSlot = builder.addOutputSlot(91, 19).setOutputSlotBackground();
        IIngredientAcceptor<?> outputIngredientSlot = outputSlot;
        outputIngredientSlot.addItemStack(recipe.getDisplayResult());
        builder.moveRecipeTransferButton(106, 57);
    }

    @Override
    public void draw(CrownBuilderRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        graphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei.glitchedcrown.required_tier", roman(recipe.getRequiredTier())),
                1, 61, 0x404040, false);
    }

    static String roman(int tier) {
        return switch (tier) { case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; default -> "I"; };
    }
}
