package mezz.jei.api.recipe;
import java.util.List;
public interface IRecipeManager {
    <T> void addRecipes(RecipeType<T> recipeType, List<T> recipes);
}
