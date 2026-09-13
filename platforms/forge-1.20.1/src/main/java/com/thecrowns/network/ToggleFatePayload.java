package com.thecrowns.network;

import com.thecrowns.logic.CrownLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ToggleFatePayload {
    public static void encode(ToggleFatePayload message, FriendlyByteBuf buffer) {
    }

    public static ToggleFatePayload decode(FriendlyByteBuf buffer) {
        return new ToggleFatePayload();
    }

    public static void handle(ToggleFatePayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) CrownLogic.toggleFate(player);
        });
        context.setPacketHandled(true);
    }
}
