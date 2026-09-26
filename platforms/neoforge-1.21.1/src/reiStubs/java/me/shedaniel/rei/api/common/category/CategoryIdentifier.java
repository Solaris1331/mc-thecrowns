package me.shedaniel.rei.api.common.category;
import me.shedaniel.rei.api.common.display.Display;
import net.minecraft.resources.ResourceLocation;
public interface CategoryIdentifier<T extends Display> {
    static <T extends Display> CategoryIdentifier<T> of(String namespace, String path) { return null; }
    static <T extends Display> CategoryIdentifier<T> of(ResourceLocation id) { return null; }
}
