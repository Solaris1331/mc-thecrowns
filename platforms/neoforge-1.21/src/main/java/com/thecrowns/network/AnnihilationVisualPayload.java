package com.thecrowns.network;

import com.thecrowns.client.ClientCrownState;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.DistExecutor;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client-bound notification for the configurable expanding annihilation shell. */
public record AnnihilationVisualPayload(double x, double y, double z, double radius) {
    public static void encode(AnnihilationVisualPayload message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.x); buffer.writeDouble(message.y); buffer.writeDouble(message.z); buffer.writeDouble(message.radius);
    }
    public static AnnihilationVisualPayload decode(FriendlyByteBuf buffer) {
        return new AnnihilationVisualPayload(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }
    public static void handle(AnnihilationVisualPayload message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientCrownState.startAnnihilation(message.x, message.y, message.z, message.radius)));
        context.setPacketHandled(true);
    }
}
