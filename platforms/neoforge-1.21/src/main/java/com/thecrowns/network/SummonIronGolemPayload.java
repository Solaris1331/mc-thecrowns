package com.thecrowns.network;

import com.thecrowns.logic.NewCrownLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class SummonIronGolemPayload {
    public static void encode(SummonIronGolemPayload message, FriendlyByteBuf buffer) {}
    public static SummonIronGolemPayload decode(FriendlyByteBuf buffer) { return new SummonIronGolemPayload(); }

    public static void handle(SummonIronGolemPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && NewCrownLogic.isWearingIronforged(player)) {
                NewCrownLogic.summonIronGolem(player);
            }
        });
        context.setPacketHandled(true);
    }
}
