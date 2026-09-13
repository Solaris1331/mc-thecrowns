package mezz.jei.api;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
public interface IModPlugin {
    ResourceLocation getPluginUid();
    default void registerCategories(IRecipeCategoryRegistration registration) {}
    default void registerRecipes(IRecipeRegistration registration) {}
    default void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {}
    default void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {}
    default void onRuntimeAvailable(IJeiRuntime jeiRuntime) {}
}
