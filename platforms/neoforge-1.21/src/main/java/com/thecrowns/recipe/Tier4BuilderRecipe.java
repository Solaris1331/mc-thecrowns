package com.thecrowns.recipe;

import com.thecrowns.registry.ModItems;
import com.thecrowns.registry.ModRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class Tier4BuilderRecipe extends AbstractBuilderUpgradeRecipe {
    /** Original design: Netherite corners, Compressed Emerald edges, Builder III center. */
    private static final Ingredient[] INGREDIENTS = {
            Ingredient.of(Items.NETHERITE_BLOCK), Ingredient.of(ModItems.COMPRESSED_EMERALD_BLOCK.get()), Ingredient.of(Items.NETHERITE_BLOCK),
            Ingredient.of(ModItems.COMPRESSED_EMERALD_BLOCK.get()), Ingredient.EMPTY, Ingredient.of(ModItems.COMPRESSED_EMERALD_BLOCK.get()),
            Ingredient.of(Items.NETHERITE_BLOCK), Ingredient.of(ModItems.COMPRESSED_EMERALD_BLOCK.get()), Ingredient.of(Items.NETHERITE_BLOCK)
    };

    public Tier4BuilderRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category, ModItems.CROWN_BUILDER_T4.get(), ModItems.CROWN_BUILDER_T3.get(), INGREDIENTS);
    }

    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.TIER4_BUILDER_UPGRADE.get(); }
}
