package com.thecrowns.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** One registered 1.21 transport payload containing the legacy Crown message discriminator. */
public record CrownPayload(Object message) implements CustomPacketPayload {
    public static final Type<CrownPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("thecrowns", "main"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CrownPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> ModNetworking.encode(buffer, payload.message()), ModNetworking::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
