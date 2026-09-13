package com.glitchedcrown.recipe;

import com.glitchedcrown.registry.ModItems;
import com.glitchedcrown.config.CrownServerConfig;
import com.glitchedcrown.registry.ModRecipes;
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
import net.minecraft.world.level.Level;

/**
 * Exact 3x3 PvP Glitched Crown recipe.  The center Dragon Egg is returned as a
 * crafting remainder only when no loaded recipe produces additional Dragon Eggs.
 */
public final class GlitchedCrownRecipe extends CustomRecipe {
    private static final NonNullList<Ingredient> INGREDIENTS = NonNullList.of(
            Ingredient.EMPTY,
            Ingredient.of(Items.NETHERITE_BLOCK),
            Ingredient.of(Items.NETHER_STAR),
            Ingredient.of(Items.CONDUIT),
            Ingredient.of(Items.SPONGE),
            Ingredient.of(Items.DRAGON_EGG),
            Ingredient.of(Items.SCULK_CATALYST),
            Ingredient.of(Items.TORCHFLOWER),
            Ingredient.of(Items.SNIFFER_EGG),
            Ingredient.of(Items.ENCHANTED_GOLDEN_APPLE)
    );

    public GlitchedCrownRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (!CrownServerConfig.GLITCHED_RECIPE.get()) return false;
        if (container.getWidth() != 3 || container.getHeight() != 3) return false;
        for (int i = 0; i < 9; i++) {
            if (!INGREDIENTS.get(i).test(container.getItem(i))) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        return new ItemStack(ModItems.GLITCHED_CROWN.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return new ItemStack(ModItems.GLITCHED_CROWN.get());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return INGREDIENTS;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < remaining.size(); i++) {
            ItemStack input = container.getItem(i);
            if (input.hasCraftingRemainingItem()) {
                remaining.set(i, input.getCraftingRemainingItem());
            }
        }

        if (container.getContainerSize() > 4
                && container.getItem(4).is(Items.DRAGON_EGG)) {
            remaining.set(4, new ItemStack(Items.DRAGON_EGG));
        }
        return remaining;
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.GLITCHED_CROWN.get();
    }
}
