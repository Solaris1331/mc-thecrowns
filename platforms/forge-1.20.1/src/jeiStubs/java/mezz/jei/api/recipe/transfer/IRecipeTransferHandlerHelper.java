package mezz.jei.api.recipe.transfer;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
public interface IRecipeTransferHandlerHelper {
    IRecipeTransferError createUserErrorWithTooltip(Component message);
    <C extends AbstractContainerMenu,R> IRecipeTransferInfo<C,R> createBasicRecipeTransferInfo(Class<? extends C> containerClass, MenuType<C> menuType, RecipeType<R> recipeType, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount);
    <C extends AbstractContainerMenu,R> IRecipeTransferHandler<C,R> createUnregisteredRecipeTransferHandler(IRecipeTransferInfo<C,R> info);
}
