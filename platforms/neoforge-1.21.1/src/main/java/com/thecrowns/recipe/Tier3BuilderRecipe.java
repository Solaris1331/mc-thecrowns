package com.thecrowns.recipe;

import com.thecrowns.registry.ModItems;
import com.thecrowns.registry.ModRecipes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class Tier3BuilderRecipe extends AbstractBuilderUpgradeRecipe {
    private static final Ingredient[] INGREDIENTS = {
            Ingredient.of(Items.DIAMOND_BLOCK), Ingredient.of(Items.DRAGON_HEAD), Ingredient.of(Items.DIAMOND_BLOCK),
            Ingredient.of(Items.END_ROD), Ingredient.EMPTY, Ingredient.of(Items.END_ROD),
            Ingredient.of(Items.DIAMOND_BLOCK), Ingredient.of(Items.SHULKER_SHELL), Ingredient.of(Items.DIAMOND_BLOCK)
    };

    public Tier3BuilderRecipe(CraftingBookCategory category) {
        super(category, ModItems.CROWN_BUILDER_T3.get(), ModItems.CROWN_BUILDER_T2.get(), INGREDIENTS);
    }

    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.TIER3_BUILDER_UPGRADE.get(); }
}
