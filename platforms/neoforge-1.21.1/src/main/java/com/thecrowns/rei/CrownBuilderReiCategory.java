package com.thecrowns.rei;

import com.thecrowns.registry.ModItems;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

final class CrownBuilderReiCategory implements DisplayCategory<CrownBuilderReiDisplay> {
    @Override public CategoryIdentifier<? extends CrownBuilderReiDisplay> getCategoryIdentifier() { return TheCrownsReiPlugin.CROWN_BUILDER; }
    @Override public Component getTitle() { return Component.translatable("jei.thecrowns.crown_builder.title"); }
    @Override public Renderer getIcon() { return EntryIngredients.of(ModItems.CROWN_BUILDER_T1.get()).get(0); }
    @Override public int getDisplayWidth(CrownBuilderReiDisplay display) { return 128; }
    @Override public int getDisplayHeight() { return 78; }

    @Override
    public List<Widget> setupDisplay(CrownBuilderReiDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        int x = bounds.x + 5;
        int y = bounds.y + 5;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                Slot slot = Widgets.createSlot(new Point(x + col * 18, y + row * 18));
                widgets.add(slot.entries(display.getInputEntries().get(index)).markInput());
            }
        }
        widgets.add(Widgets.createArrow(new Point(x + 62, y + 18)));
        Slot output = Widgets.createSlot(new Point(x + 94, y + 18));
        widgets.add(output.entries(display.getOutputEntries().get(0)).markOutput());
        widgets.add(Widgets.createLabel(new Point(x, y + 58), Component.translatable(
                "jei.thecrowns.required_tier", roman(display.source().getRequiredTier()))));
        return widgets;
    }

    private static String roman(int tier) {
        return switch (tier) { case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; default -> "I"; };
    }
}
