package com.thecrowns.recipe;

import com.thecrowns.logic.CrownAdvancementGate;
import com.thecrowns.config.CrownServerConfig;
import com.thecrowns.mixin.CraftingMenuAccessor;
import com.thecrowns.mixin.InventoryMenuAccessor;
import com.thecrowns.mixin.TransientCraftingContainerAccessor;
import com.thecrowns.registry.ModItems;
import com.thecrowns.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
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

    public UnleashedCrownRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (!CrownServerConfig.UNLEASHED_RECIPE.get()) return false;
        if (container.getWidth() != 3 || container.getHeight() != 3) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = container.getItem(i);
            if (i == 6) { // bottom-left: Uncraftable Potion = minecraft:empty potion
                if (!stack.is(Items.POTION) || PotionUtils.getPotion(stack) != Potions.EMPTY) return false;
            } else if (!INGREDIENTS.get(i).test(stack)) {
                return false;
            }
        }

        // Client-side crafting grids receive their actual output from the server;
        // do not suppress their local shape test here. The authoritative server
        // path below requires the acquisition advancement.
        if (level.isClientSide) return true;
        Player owner = owner(container);
        if (!CrownServerConfig.UNLEASHED_REQUIRE_GLITCHED_ADVANCEMENT.get()) return true;
        return owner instanceof ServerPlayer serverPlayer
                && CrownAdvancementGate.hasGlitchedCrownAdvancement(serverPlayer);
    }

    private static Player owner(CraftingContainer container) {
        if (!(container instanceof TransientCraftingContainer transientContainer)) return null;
        AbstractContainerMenu menu = ((TransientCraftingContainerAccessor) transientContainer).thecrowns$getMenu();
        if (menu instanceof CraftingMenu craftingMenu) {
            return ((CraftingMenuAccessor) craftingMenu).thecrowns$getPlayer();
        }
        if (menu instanceof InventoryMenu inventoryMenu) {
            return ((InventoryMenuAccessor) inventoryMenu).thecrowns$getOwner();
        }
        return null;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        return new ItemStack(ModItems.UNLEASHED_CROWN.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
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
