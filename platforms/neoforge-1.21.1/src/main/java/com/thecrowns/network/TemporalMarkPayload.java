package com.thecrowns.network;

import com.thecrowns.logic.AdvancedCrownLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.thecrowns.network.legacy.NetworkEvent;

import java.util.function.Supplier;

public final class TemporalMarkPayload {
    public static void encode(TemporalMarkPayload message, FriendlyByteBuf buffer) {}
    public static TemporalMarkPayload decode(FriendlyByteBuf buffer) { return new TemporalMarkPayload(); }

    public static void handle(TemporalMarkPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) AdvancedCrownLogic.markTemporalPosition(player);
        });
        context.setPacketHandled(true);
    }
}
