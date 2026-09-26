package com.thecrowns.network;

import com.thecrowns.logic.NewCrownLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class AngelicFlightPayload {
    public static void encode(AngelicFlightPayload message, FriendlyByteBuf buffer) {}
    public static AngelicFlightPayload decode(FriendlyByteBuf buffer) { return new AngelicFlightPayload(); }

    public static void handle(AngelicFlightPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) NewCrownLogic.activateAngelicFlight(player);
        });
        context.setPacketHandled(true);
    }
}
