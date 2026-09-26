package com.thecrowns.recipe;

import com.thecrowns.registry.ModItems;
import com.thecrowns.registry.ModRecipes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class Tier2BuilderRecipe extends AbstractBuilderUpgradeRecipe {
    private static final Ingredient[] INGREDIENTS = {
            Ingredient.of(Items.GOLD_BLOCK), Ingredient.of(Items.ENCHANTING_TABLE), Ingredient.of(Items.GOLD_BLOCK),
            Ingredient.of(Items.NETHERITE_INGOT), Ingredient.EMPTY, Ingredient.of(Items.NETHERITE_INGOT),
            Ingredient.of(Items.GOLD_BLOCK), Ingredient.of(Items.WITHER_SKELETON_SKULL), Ingredient.of(Items.GOLD_BLOCK)
    };

    public Tier2BuilderRecipe(CraftingBookCategory category) {
        super(category, ModItems.CROWN_BUILDER_T2.get(), ModItems.CROWN_BUILDER_T1.get(), INGREDIENTS);
    }

    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.TIER2_BUILDER_UPGRADE.get(); }
}
