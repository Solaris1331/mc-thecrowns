package me.shedaniel.rei.api.client.plugins;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
public interface REIClientPlugin {
    default void registerCategories(CategoryRegistry registry) {}
    default void registerDisplays(DisplayRegistry registry) {}
    default void registerTransferHandlers(TransferHandlerRegistry registry) {}
}
