package com.thecrowns.recipe;

import net.minecraft.core.NonNullList;
import com.thecrowns.registry.ModBlockEntities;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
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

    protected AbstractBuilderUpgradeRecipe(CraftingBookCategory category,
                                           Item resultItem, Item centerBuilder, Ingredient[] ingredients) {
        super(category);
        this.resultItem = resultItem;
        this.centerBuilder = centerBuilder;
        this.ingredients = ingredients;
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        if (container.width() != 3 || container.height() != 3) return false;
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
    public ItemStack assemble(CraftingInput container, HolderLookup.Provider access) {
        ItemStack result = new ItemStack(resultItem);
        ItemStack center = container.getItem(4);
        CustomData blockEntityData = center.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData != null && !blockEntityData.copyTag().isEmpty()) {
            BlockItem.setBlockEntityData(result, ModBlockEntities.CROWN_BUILDER.get(), blockEntityData.copyTag());
        }
        if (center.has(DataComponents.CUSTOM_NAME)) result.set(DataComponents.CUSTOM_NAME, center.get(DataComponents.CUSTOM_NAME));
        return result;
    }

    @Override public boolean canCraftInDimensions(int width, int height) { return width >= 3 && height >= 3; }
    @Override public ItemStack getResultItem(HolderLookup.Provider access) { return new ItemStack(resultItem); }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.withSize(9, Ingredient.EMPTY);
        for (int i = 0; i < 9; i++) list.set(i, i == 4 ? Ingredient.of(centerBuilder) : ingredients[i]);
        return list;
    }

    @Override public boolean isSpecial() { return false; }
}
