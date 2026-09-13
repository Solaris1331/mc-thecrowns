package com.glitchedcrown.network;

import com.glitchedcrown.logic.AdvancedCrownLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class CursedLiberationPayload {
    public static void encode(CursedLiberationPayload message, FriendlyByteBuf buffer) {}
    public static CursedLiberationPayload decode(FriendlyByteBuf buffer) { return new CursedLiberationPayload(); }

    public static void handle(CursedLiberationPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && AdvancedCrownLogic.isWearingCursed(player)) {
                AdvancedCrownLogic.activateLiberation(player);
            }
        });
        context.setPacketHandled(true);
    }
}
