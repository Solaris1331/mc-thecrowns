package com.thecrowns.network;

import com.thecrowns.logic.CrownLogic;
import com.thecrowns.logic.GlitchedFusionLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ToggleAuraPayload {
    public static void encode(ToggleAuraPayload message, FriendlyByteBuf buffer) {
    }

    public static ToggleAuraPayload decode(FriendlyByteBuf buffer) {
        return new ToggleAuraPayload();
    }

    public static void handle(ToggleAuraPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && !GlitchedFusionLogic.activateJAbility(player)) CrownLogic.toggleAura(player);
        });
        context.setPacketHandled(true);
    }
}
