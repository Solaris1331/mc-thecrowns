package com.thecrowns.network;

import com.thecrowns.logic.CrownLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class FireLaserPayload {
    public static void encode(FireLaserPayload message, FriendlyByteBuf buffer) {
    }

    public static FireLaserPayload decode(FriendlyByteBuf buffer) {
        return new FireLaserPayload();
    }

    public static void handle(FireLaserPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) CrownLogic.fireLaser(player);
        });
        context.setPacketHandled(true);
    }
}
