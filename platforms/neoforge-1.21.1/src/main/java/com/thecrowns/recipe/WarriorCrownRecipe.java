package com.thecrowns.recipe;

import com.thecrowns.registry.ModItems;
import com.thecrowns.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.thecrowns.util.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Exact Warrior Crown recipe. The top-left potion must be Strength II. */
public final class WarriorCrownRecipe extends CustomRecipe {
    private static final NonNullList<Ingredient> INGREDIENTS = NonNullList.of(
            Ingredient.EMPTY,
            Ingredient.of(Items.POTION), Ingredient.of(Items.BLAZE_ROD), Ingredient.of(Items.NETHERITE_SWORD),
            Ingredient.of(Items.IRON_BLOCK), Ingredient.of(Items.ANCIENT_DEBRIS), Ingredient.of(Items.IRON_BLOCK),
            Ingredient.of(Items.NETHERITE_AXE), Ingredient.of(Items.ANVIL), Ingredient.of(Items.TRIDENT)
    );

    public WarriorCrownRecipe(CraftingBookCategory category) { super(category); }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        if (container.width() != 3 || container.height() != 3) return false;
        for (int i = 0; i < 9; i++) if (!INGREDIENTS.get(i).test(container.getItem(i))) return false;
        return PotionUtils.getPotion(container.getItem(0)) == Potions.STRONG_STRENGTH;
    }

    @Override public ItemStack assemble(CraftingInput container, HolderLookup.Provider access) { return new ItemStack(ModItems.WARRIOR_CROWN.get()); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width >= 3 && height >= 3; }
    @Override public ItemStack getResultItem(HolderLookup.Provider access) { return new ItemStack(ModItems.WARRIOR_CROWN.get()); }
    @Override public NonNullList<Ingredient> getIngredients() { return INGREDIENTS; }
    @Override public boolean isSpecial() { return false; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.WARRIOR_CROWN.get(); }
}
