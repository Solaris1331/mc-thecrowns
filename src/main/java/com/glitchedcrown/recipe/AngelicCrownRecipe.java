package com.glitchedcrown.recipe;

import com.glitchedcrown.registry.ModItems;
import com.glitchedcrown.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Exact Angelic Crown recipe with Slow Falling and Leaping potions in the bottom corners. */
public final class AngelicCrownRecipe extends CustomRecipe {
    private static final NonNullList<Ingredient> INGREDIENTS = NonNullList.of(
            Ingredient.EMPTY,
            Ingredient.of(Items.FEATHER), Ingredient.of(Items.HONEYCOMB_BLOCK), Ingredient.of(Items.FEATHER),
            Ingredient.of(Items.SHULKER_SHELL), Ingredient.of(Items.ELYTRA), Ingredient.of(Items.SHULKER_SHELL),
            Ingredient.of(Items.POTION), Ingredient.of(Items.DIAMOND_BLOCK), Ingredient.of(Items.POTION)
    );

    public AngelicCrownRecipe(ResourceLocation id, CraftingBookCategory category) { super(id, category); }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (container.getWidth() != 3 || container.getHeight() != 3) return false;
        for (int i = 0; i < 9; i++) if (!INGREDIENTS.get(i).test(container.getItem(i))) return false;
        return PotionUtils.getPotion(container.getItem(6)) == Potions.SLOW_FALLING
                && PotionUtils.getPotion(container.getItem(8)) == Potions.LEAPING;
    }

    @Override public ItemStack assemble(CraftingContainer container, RegistryAccess access) { return new ItemStack(ModItems.ANGELIC_CROWN.get()); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width >= 3 && height >= 3; }
    @Override public ItemStack getResultItem(RegistryAccess access) { return new ItemStack(ModItems.ANGELIC_CROWN.get()); }
    @Override public NonNullList<Ingredient> getIngredients() { return INGREDIENTS; }
    @Override public boolean isSpecial() { return false; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.ANGELIC_CROWN.get(); }
}
