package mezz.jei.api.registration;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.ItemLike;
public interface IRecipeCatalystRegistration { void addRecipeCatalysts(RecipeType<?> recipeType, ItemLike... ingredients); }
