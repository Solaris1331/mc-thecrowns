package me.shedaniel.rei.api.common.util;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
public final class EntryIngredients {
    private EntryIngredients() {}
    public static EntryIngredient of(ItemLike item) { return null; }
    public static EntryIngredient of(ItemStack stack) { return null; }
    public static EntryIngredient ofIngredient(Ingredient ingredient) { return null; }
}
