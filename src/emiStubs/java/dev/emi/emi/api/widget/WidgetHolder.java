package dev.emi.emi.api.widget;
import dev.emi.emi.api.stack.EmiIngredient;
public interface WidgetHolder {
    Object addSlot(EmiIngredient ingredient, int x, int y);
}
