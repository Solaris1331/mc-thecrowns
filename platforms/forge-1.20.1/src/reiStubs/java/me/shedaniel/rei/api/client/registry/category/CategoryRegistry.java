package me.shedaniel.rei.api.client.registry.category;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
public interface CategoryRegistry {
    void add(DisplayCategory<?> category);
    <D extends Display> void addWorkstations(CategoryIdentifier<D> category, EntryIngredient... stations);
}
