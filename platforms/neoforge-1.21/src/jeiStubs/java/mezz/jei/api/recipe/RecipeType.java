package mezz.jei.api.recipe;
import net.minecraft.resources.ResourceLocation;
public final class RecipeType<T> {
    public static <T> RecipeType<T> create(String namespace, String path, Class<? extends T> recipeClass) { return new RecipeType<>(ResourceLocation.fromNamespaceAndPath(namespace, path), recipeClass); }
    public RecipeType(ResourceLocation uid, Class<? extends T> recipeClass) {}
}
