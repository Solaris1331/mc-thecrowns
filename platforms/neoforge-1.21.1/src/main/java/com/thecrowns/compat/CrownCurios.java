package com.thecrowns.compat;

import com.thecrowns.config.CrownServerConfig;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;

public final class CrownCurios {
    public static final String CROWN_SLOT = "crown";

    private CrownCurios() {
    }

    public static void enqueueImc(InterModEnqueueEvent event) {
        InterModComms.sendTo(
                "curios",
                SlotTypeMessage.REGISTER_TYPE,
                () -> new SlotTypeMessage.Builder(CROWN_SLOT).size(1).build()
        );
    }

    public static void syncSlotCount(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;

        int desiredSlots = CrownServerConfig.CROWN_SLOT_COUNT.get();
        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                handler.getStacksHandler(CROWN_SLOT).ifPresent(stacks -> {
                    int difference = desiredSlots - stacks.getSlots();
                    if (difference > 0) {
                        handler.growSlotType(CROWN_SLOT, difference);
                    } else if (difference < 0) {
                        handler.shrinkSlotType(CROWN_SLOT, -difference);
                    }
                })
        );
    }
}
