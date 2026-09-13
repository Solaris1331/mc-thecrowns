package me.shedaniel.rei.api.client.gui.widgets;
import me.shedaniel.rei.api.common.entry.EntryStack;
import java.util.Collection;
public interface Slot extends Widget {
    Slot entries(Collection<? extends EntryStack<?>> stacks);
    Slot markInput();
    Slot markOutput();
}
