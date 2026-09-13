package me.shedaniel.rei.api.client.registry.display;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import net.minecraft.network.chat.Component;
import java.util.List;
public interface DisplayCategory<T extends Display> {
    CategoryIdentifier<? extends T> getCategoryIdentifier();
    Component getTitle();
    Renderer getIcon();
    List<Widget> setupDisplay(T display, Rectangle bounds);
    default int getDisplayHeight() { return 66; }
    default int getDisplayWidth(T display) { return 150; }
}
