package me.shedaniel.rei.api.client.registry.display;
import me.shedaniel.rei.api.common.display.Display;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import java.util.function.Function;
public interface DisplayRegistry {
    <T extends Recipe<?>, D extends Display> void registerRecipeFiller(Class<T> typeClass, RecipeType<? super T> recipeType, Function<? super T, D> filler);
}
