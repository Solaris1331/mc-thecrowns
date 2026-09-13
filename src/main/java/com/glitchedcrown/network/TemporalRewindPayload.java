package com.glitchedcrown.network;

import com.glitchedcrown.logic.AdvancedCrownLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class TemporalRewindPayload {
    public static void encode(TemporalRewindPayload message, FriendlyByteBuf buffer) {}
    public static TemporalRewindPayload decode(FriendlyByteBuf buffer) { return new TemporalRewindPayload(); }

    public static void handle(TemporalRewindPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) AdvancedCrownLogic.rewindTemporal(player);
        });
        context.setPacketHandled(true);
    }
}
