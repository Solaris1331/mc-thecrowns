package dev.emi.emi.api.widget;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.network.chat.Component;
public interface WidgetHolder {
    Object addSlot(EmiIngredient ingredient, int x, int y);
    Object addText(Component text, int x, int y, int color, boolean shadow);
}
