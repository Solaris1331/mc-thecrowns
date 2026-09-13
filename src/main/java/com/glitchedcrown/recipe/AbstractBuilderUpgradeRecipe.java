package com.glitchedcrown.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/** Upgrade recipe that carries the old Builder's BlockEntityTag and custom name into the new tier. */
abstract class AbstractBuilderUpgradeRecipe extends CustomRecipe {
    private final Item resultItem;
    private final Item centerBuilder;
    private final Ingredient[] ingredients;

    protected AbstractBuilderUpgradeRecipe(ResourceLocation id, CraftingBookCategory category,
                                           Item resultItem, Item centerBuilder, Ingredient[] ingredients) {
        super(id, category);
        this.resultItem = resultItem;
        this.centerBuilder = centerBuilder;
        this.ingredients = ingredients;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (container.getWidth() != 3 || container.getHeight() != 3) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = container.getItem(i);
            if (i == 4) {
                if (!stack.is(centerBuilder)) return false;
            } else if (!ingredients[i].test(stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        ItemStack result = new ItemStack(resultItem);
        ItemStack center = container.getItem(4);
        CompoundTag blockEntityTag = center.getTagElement("BlockEntityTag");
        if (blockEntityTag != null && !blockEntityTag.isEmpty()) {
            result.getOrCreateTag().put("BlockEntityTag", blockEntityTag.copy());
        }
        if (center.hasCustomHoverName()) result.setHoverName(center.getHoverName());
        return result;
    }

    @Override public boolean canCraftInDimensions(int width, int height) { return width >= 3 && height >= 3; }
    @Override public ItemStack getResultItem(RegistryAccess access) { return new ItemStack(resultItem); }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.withSize(9, Ingredient.EMPTY);
        for (int i = 0; i < 9; i++) list.set(i, i == 4 ? Ingredient.of(centerBuilder) : ingredients[i]);
        return list;
    }

    @Override public boolean isSpecial() { return false; }
}
