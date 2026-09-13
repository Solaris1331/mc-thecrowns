package mezz.jei.api.gui.builder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
@SuppressWarnings("unchecked")
public interface IIngredientAcceptor<THIS extends IIngredientAcceptor<THIS>> {
    default THIS addIngredients(Ingredient ingredient) { return (THIS) this; }
    default THIS addItemStack(ItemStack stack) { return (THIS) this; }
}
