package dev.emi.emi.api.stack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
public abstract class EmiStack implements EmiIngredient {
    public static EmiStack of(ItemLike item) { return null; }
    public static EmiStack of(ItemStack stack) { return null; }
}
