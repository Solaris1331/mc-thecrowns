package com.thecrowns.client.screen;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.menu.CrownBuilderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class CrownBuilderScreen extends AbstractContainerScreen<CrownBuilderMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TheCrownsMod.MOD_ID, "textures/gui/crown_builder.png");

    public CrownBuilderScreen(CrownBuilderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 180;
        this.titleLabelX = 8;
        this.titleLabelY = 7;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 86;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Component builderTitle = Component.translatable("gui.thecrowns.crown_builder.title");
        graphics.drawString(font, builderTitle, titleLabelX, titleLabelY, 0x404040, false);

        Component tierText = Component.translatable(
                "gui.thecrowns.crown_builder.tier", roman(menu.getBuilderTier()));
        int tierX = imageWidth - 8 - font.width(tierText);
        graphics.drawString(font, tierText, tierX, titleLabelY, 0x404040, false);

        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    private static String roman(int tier) {
        return switch (tier) { case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; default -> "I"; };
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
