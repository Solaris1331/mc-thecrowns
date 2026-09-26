package com.thecrowns.recipe;

import com.thecrowns.config.CrownServerConfig;
import com.thecrowns.registry.ModItems;
import com.thecrowns.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.thecrowns.util.PotionUtils;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Advancement-gated Unleashed Crown recipe.  The recipe is globally loaded so
 * the server can validate it, but the actual crafting result only exists for a
 * player who has earned thecrowns:glitched_crown.
 */
public final class UnleashedCrownRecipe extends CustomRecipe {
    private static final NonNullList<Ingredient> INGREDIENTS = NonNullList.of(
            Ingredient.EMPTY,
            Ingredient.of(Items.CHAIN_COMMAND_BLOCK),
            Ingredient.of(Items.REPEATING_COMMAND_BLOCK),
            Ingredient.of(Items.COMMAND_BLOCK),
            Ingredient.of(Items.BEDROCK),
            Ingredient.of(ModItems.GLITCHED_CROWN.get()),
            Ingredient.of(Items.BARRIER),
            Ingredient.of(Items.POTION),
            Ingredient.of(Items.END_PORTAL_FRAME),
            Ingredient.of(Items.SPAWNER)
    );

    public UnleashedCrownRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        if (!CrownServerConfig.UNLEASHED_RECIPE.get()) return false;
        if (container.width() != 3 || container.height() != 3) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = container.getItem(i);
            if (i == 6) { // bottom-left: Uncraftable Potion = minecraft:empty potion
                if (!stack.is(Items.POTION) || !PotionUtils.hasNoPotion(stack)) return false;
            } else if (!INGREDIENTS.get(i).test(stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput container, HolderLookup.Provider registryAccess) {
        return new ItemStack(ModItems.UNLEASHED_CROWN.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return new ItemStack(ModItems.UNLEASHED_CROWN.get());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return INGREDIENTS;
    }

    @Override
    public boolean isSpecial() {
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.UNLEASHED_CROWN.get();
    }
}
