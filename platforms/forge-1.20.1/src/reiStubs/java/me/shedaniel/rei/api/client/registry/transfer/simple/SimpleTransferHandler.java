package me.shedaniel.rei.api.client.registry.transfer.simple;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import net.minecraft.world.inventory.AbstractContainerMenu;
public interface SimpleTransferHandler extends TransferHandler {
    record IntRange(int min, int maxExclusive) {}
    static <C extends AbstractContainerMenu, D extends Display> SimpleTransferHandler create(
            Class<? extends C> containerClass, CategoryIdentifier<D> categoryIdentifier, IntRange inputSlots) { return null; }
    static <C extends AbstractContainerMenu, D extends Display> SimpleTransferHandler create(
            Class<? extends C> containerClass, CategoryIdentifier<D> categoryIdentifier,
            IntRange inputSlots, IntRange inventorySlots) { return null; }
}
