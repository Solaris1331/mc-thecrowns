package com.thecrowns.network;

import com.thecrowns.logic.AdvancedCrownLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.thecrowns.network.legacy.NetworkEvent;

import java.util.function.Supplier;

public final class TimeWarpPayload {
    public static void encode(TimeWarpPayload message, FriendlyByteBuf buffer) {}
    public static TimeWarpPayload decode(FriendlyByteBuf buffer) { return new TimeWarpPayload(); }

    public static void handle(TimeWarpPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) AdvancedCrownLogic.activateTimeWarp(player);
        });
        context.setPacketHandled(true);
    }
}
