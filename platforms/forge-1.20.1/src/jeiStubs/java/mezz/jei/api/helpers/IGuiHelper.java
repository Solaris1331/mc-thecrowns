package mezz.jei.api.helpers;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import net.minecraft.world.item.ItemStack;
public interface IGuiHelper {
    IDrawableStatic createBlankDrawable(int width, int height);
    IDrawable createDrawableItemStack(ItemStack stack);
}
