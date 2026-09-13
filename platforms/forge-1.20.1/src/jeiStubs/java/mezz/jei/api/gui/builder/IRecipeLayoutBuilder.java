package mezz.jei.api.gui.builder;
public interface IRecipeLayoutBuilder {
    IRecipeSlotBuilder addInputSlot(int x, int y);
    IRecipeSlotBuilder addOutputSlot(int x, int y);
    void moveRecipeTransferButton(int x, int y);
}
