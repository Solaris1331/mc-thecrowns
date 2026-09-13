package com.thecrowns.recipe;

import com.thecrowns.registry.ModItems;
import com.thecrowns.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/** Exact Dimensional Crown recipe; the book slot must contain at least one real enchantment. */
public final class DimensionalCrownRecipe extends CustomRecipe {
    private static final NonNullList<Ingredient> INGREDIENTS = NonNullList.of(
            Ingredient.EMPTY,
            Ingredient.of(Items.ENDER_EYE),
            Ingredient.of(Items.POPPED_CHORUS_FRUIT),
            Ingredient.of(Items.ENDER_EYE),
            Ingredient.of(Items.ENCHANTING_TABLE),
            Ingredient.of(Items.DRAGON_HEAD),
            Ingredient.of(Items.ENCHANTED_BOOK),
            Ingredient.of(Items.ENDER_EYE),
            Ingredient.of(Items.CHORUS_FLOWER),
            Ingredient.of(Items.ENDER_EYE)
    );

    public DimensionalCrownRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (container.getWidth() != 3 || container.getHeight() != 3) return false;
        for (int i = 0; i < 9; i++) {
            if (!INGREDIENTS.get(i).test(container.getItem(i))) return false;
        }
        ItemStack book = container.getItem(5);
        return book.is(Items.ENCHANTED_BOOK) && !EnchantmentHelper.getEnchantments(book).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        return new ItemStack(ModItems.DIMENSIONAL_CROWN.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return width >= 3 && height >= 3; }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) { return new ItemStack(ModItems.DIMENSIONAL_CROWN.get()); }

    @Override
    public NonNullList<Ingredient> getIngredients() { return INGREDIENTS; }

    @Override
    public boolean isSpecial() { return false; }

    @Override
    public RecipeSerializer<?> getSerializer() { return ModRecipes.DIMENSIONAL_CROWN.get(); }
}
