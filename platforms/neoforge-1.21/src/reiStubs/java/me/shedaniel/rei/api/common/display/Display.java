package me.shedaniel.rei.api.common.display;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.Optional;
public interface Display {
    List<EntryIngredient> getInputEntries();
    List<EntryIngredient> getOutputEntries();
    CategoryIdentifier<?> getCategoryIdentifier();
    default Optional<ResourceLocation> getDisplayLocation() { return Optional.empty(); }
}
