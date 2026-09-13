package com.glitchedcrown.network;

import com.glitchedcrown.client.ClientCrownState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client-bound Glitched Crown revival overlay, analogous to totem item activation. */
public final class NerfedReviveEffectPayload {
    public static void encode(NerfedReviveEffectPayload message, FriendlyByteBuf buffer) {
    }

    public static NerfedReviveEffectPayload decode(FriendlyByteBuf buffer) {
        return new NerfedReviveEffectPayload();
    }

    public static void handle(NerfedReviveEffectPayload message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> ClientCrownState::showGlitchedReviveActivation));
        context.setPacketHandled(true);
    }
}
