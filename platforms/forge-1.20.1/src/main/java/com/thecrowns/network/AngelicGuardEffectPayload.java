package com.thecrowns.network;

import com.thecrowns.client.ClientCrownState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client-bound Angelic Crown lethal-guard activation overlay, analogous to totem item activation. */
public final class AngelicGuardEffectPayload {
    public static void encode(AngelicGuardEffectPayload message, FriendlyByteBuf buffer) {
    }

    public static AngelicGuardEffectPayload decode(FriendlyByteBuf buffer) {
        return new AngelicGuardEffectPayload();
    }

    public static void handle(AngelicGuardEffectPayload message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> ClientCrownState::showAngelicGuardActivation));
        context.setPacketHandled(true);
    }
}
