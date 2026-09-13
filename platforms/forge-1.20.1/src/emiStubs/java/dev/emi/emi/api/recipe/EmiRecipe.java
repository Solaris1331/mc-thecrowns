package dev.emi.emi.api.recipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
public interface EmiRecipe {
    EmiRecipeCategory getCategory();
    ResourceLocation getId();
    List<EmiIngredient> getInputs();
    List<EmiStack> getOutputs();
    int getDisplayWidth();
    int getDisplayHeight();
    void addWidgets(WidgetHolder widgets);
}
