package com.thecrowns.recipe;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** Adapts the Builder's private nine-slot inventory to the 1.21 recipe API. */
public record CrownBuilderRecipeInput(Container container) implements RecipeInput {
    public static CrownBuilderRecipeInput of(Container container) {
        return new CrownBuilderRecipeInput(container);
    }

    @Override
    public ItemStack getItem(int index) {
        return container.getItem(index);
    }

    @Override
    public int size() {
        return container.getContainerSize();
    }
}
