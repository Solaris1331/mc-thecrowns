package com.glitchedcrown.rei;

import com.glitchedcrown.recipe.CrownBuilderRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class CrownBuilderReiDisplay implements Display {
    private final CrownBuilderRecipe recipe;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;

    CrownBuilderReiDisplay(CrownBuilderRecipe recipe) {
        this.recipe = recipe;
        this.inputs = new ArrayList<>(9);
        for (int slot = 0; slot < 9; slot++) {
            ItemStack special = recipe.getSpecialDisplayStack(slot);
            if (!special.isEmpty()) inputs.add(EntryIngredients.of(special));
            else inputs.add(EntryIngredients.ofIngredient(recipe.getIngredients().get(slot)));
        }
        this.outputs = List.of(EntryIngredients.of(recipe.getDisplayResult()));
    }

    CrownBuilderRecipe source() { return recipe; }
    @Override public List<EntryIngredient> getInputEntries() { return inputs; }
    @Override public List<EntryIngredient> getOutputEntries() { return outputs; }
    @Override public CategoryIdentifier<?> getCategoryIdentifier() { return TheCrownsReiPlugin.CROWN_BUILDER; }
    @Override public Optional<ResourceLocation> getDisplayLocation() { return Optional.of(recipe.getId()); }
}
