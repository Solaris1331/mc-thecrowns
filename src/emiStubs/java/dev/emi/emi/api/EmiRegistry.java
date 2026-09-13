package dev.emi.emi.api;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeManager;
public interface EmiRegistry {
    void addCategory(EmiRecipeCategory category);
    void addWorkstation(EmiRecipeCategory category, EmiIngredient workstation);
    void addRecipe(EmiRecipe recipe);
    <T extends AbstractContainerMenu> void addRecipeHandler(MenuType<T> type, EmiRecipeHandler<T> handler);
    RecipeManager getRecipeManager();
}
