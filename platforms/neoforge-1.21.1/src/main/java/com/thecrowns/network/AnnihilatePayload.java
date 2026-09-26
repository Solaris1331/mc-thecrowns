package com.thecrowns.network;

import com.thecrowns.logic.CrownLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.thecrowns.network.legacy.NetworkEvent;

import java.util.function.Supplier;

public final class AnnihilatePayload {
    public static void encode(AnnihilatePayload message, FriendlyByteBuf buffer) {
    }

    public static AnnihilatePayload decode(FriendlyByteBuf buffer) {
        return new AnnihilatePayload();
    }

    public static void handle(AnnihilatePayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) CrownLogic.annihilate(player);
        });
        context.setPacketHandled(true);
    }
}
