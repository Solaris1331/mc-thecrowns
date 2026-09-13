package dev.emi.emi.api.recipe.handler;
import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import java.util.List;
public interface StandardRecipeHandler<T extends AbstractContainerMenu> extends EmiRecipeHandler<T> {
    List<Slot> getInputSources(T handler);
    List<Slot> getCraftingSlots(T handler);
    default Slot getOutputSlot(T handler) { return null; }
    default boolean supportsRecipe(EmiRecipe recipe) { return true; }
}
