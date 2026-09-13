package dev.emi.emi.api.stack;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.world.item.crafting.Ingredient;
public interface EmiIngredient extends EmiRenderable {
    static EmiIngredient of(Ingredient ingredient) { return null; }
}
