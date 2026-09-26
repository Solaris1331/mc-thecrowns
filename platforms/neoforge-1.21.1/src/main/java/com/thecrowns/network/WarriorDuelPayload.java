package com.thecrowns.network;

import com.thecrowns.logic.NewCrownLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.thecrowns.network.legacy.NetworkEvent;

import java.util.function.Supplier;

public final class WarriorDuelPayload {
    public static void encode(WarriorDuelPayload message, FriendlyByteBuf buffer) {}
    public static WarriorDuelPayload decode(FriendlyByteBuf buffer) { return new WarriorDuelPayload(); }

    public static void handle(WarriorDuelPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) NewCrownLogic.activateWarriorDuel(player);
        });
        context.setPacketHandled(true);
    }
}
