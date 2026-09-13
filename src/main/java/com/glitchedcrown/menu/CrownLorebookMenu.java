package com.glitchedcrown.menu;

import com.glitchedcrown.logic.CrownAdvancementGate;
import com.glitchedcrown.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Slotless menu used only to open/synchronize the lorebook screen safely from the server. */
public final class CrownLorebookMenu extends AbstractContainerMenu {
    private final boolean unleashedUnlocked;
    private final boolean cursedUnlocked;
    private final List<CrownAdvancementGate.AdvancementStatus> coreStatus;
    private final List<CrownAdvancementGate.AdvancementStatus> substituteStatus;

    public CrownLorebookMenu(int containerId, Inventory inventory, boolean unleashedUnlocked, boolean cursedUnlocked,
                             List<CrownAdvancementGate.AdvancementStatus> coreStatus,
                             List<CrownAdvancementGate.AdvancementStatus> substituteStatus) {
        super(ModMenus.CROWN_LOREBOOK.get(), containerId);
        this.unleashedUnlocked = unleashedUnlocked;
        this.cursedUnlocked = cursedUnlocked;
        this.coreStatus = List.copyOf(coreStatus);
        this.substituteStatus = List.copyOf(substituteStatus);
    }

    public CrownLorebookMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBoolean(), buf.readBoolean(), readStatuses(buf), readStatuses(buf));
    }

    public boolean isUnleashedUnlocked() { return unleashedUnlocked; }
    public boolean isCursedUnlocked() { return cursedUnlocked; }
    public List<CrownAdvancementGate.AdvancementStatus> coreStatus() { return coreStatus; }
    public List<CrownAdvancementGate.AdvancementStatus> substituteStatus() { return substituteStatus; }

    public static void writeOpenData(FriendlyByteBuf buf, boolean unleashedUnlocked, boolean cursedUnlocked,
                                     List<CrownAdvancementGate.AdvancementStatus> coreStatus,
                                     List<CrownAdvancementGate.AdvancementStatus> substituteStatus) {
        buf.writeBoolean(unleashedUnlocked);
        buf.writeBoolean(cursedUnlocked);
        writeStatuses(buf, coreStatus);
        writeStatuses(buf, substituteStatus);
    }

    private static void writeStatuses(FriendlyByteBuf buf, List<CrownAdvancementGate.AdvancementStatus> statuses) {
        buf.writeVarInt(statuses.size());
        for (CrownAdvancementGate.AdvancementStatus status : statuses) {
            buf.writeResourceLocation(status.id());
            buf.writeComponent(status.title());
            buf.writeBoolean(status.done());
        }
    }

    private static List<CrownAdvancementGate.AdvancementStatus> readStatuses(FriendlyByteBuf buf) {
        int size = Math.max(0, Math.min(256, buf.readVarInt()));
        List<CrownAdvancementGate.AdvancementStatus> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            out.add(new CrownAdvancementGate.AdvancementStatus(
                    buf.readResourceLocation(), buf.readComponent(), buf.readBoolean()));
        }
        return List.copyOf(out);
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
