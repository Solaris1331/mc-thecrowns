package mezz.jei.api.recipe.category;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
public interface IRecipeCategory<T> {
    RecipeType<T> getRecipeType();
    Component getTitle();
    default IDrawable getBackground() { return null; }
    default int getWidth() { IDrawable bg = getBackground(); return bg == null ? 0 : bg.getWidth(); }
    default int getHeight() { IDrawable bg = getBackground(); return bg == null ? 0 : bg.getHeight(); }
    IDrawable getIcon();
    void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses);
    default void draw(T recipe, IRecipeSlotsView view, GuiGraphics graphics, double mouseX, double mouseY) {}
}
