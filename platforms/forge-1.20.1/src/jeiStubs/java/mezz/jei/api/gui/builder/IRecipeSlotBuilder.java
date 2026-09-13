package mezz.jei.api.gui.builder;
public interface IRecipeSlotBuilder extends IIngredientAcceptor<IRecipeSlotBuilder> {
    IRecipeSlotBuilder setStandardSlotBackground();
    IRecipeSlotBuilder setOutputSlotBackground();
}
