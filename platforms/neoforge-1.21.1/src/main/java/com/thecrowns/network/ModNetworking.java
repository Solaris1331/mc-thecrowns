package com.thecrowns.network;

import com.thecrowns.network.legacy.NetworkEvent;
import com.thecrowns.network.legacy.PacketDistributor.Target;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** 1.21 payload registration, retaining the existing Crown handlers behind one typed transport. */
public final class ModNetworking {
    public static final Channel CHANNEL = new Channel();
    private static final Map<Integer, Registration<?>> BY_ID = new HashMap<>();
    private static final Map<Class<?>, Registration<?>> BY_CLASS = new HashMap<>();

    static {
        add(0, FireLaserPayload.class, FireLaserPayload::encode, FireLaserPayload::decode, FireLaserPayload::handle);
        add(1, ToggleFatePayload.class, ToggleFatePayload::encode, ToggleFatePayload::decode, ToggleFatePayload::handle);
        add(2, ToggleAuraPayload.class, ToggleAuraPayload::encode, ToggleAuraPayload::decode, ToggleAuraPayload::handle);
        add(3, AnnihilatePayload.class, AnnihilatePayload::encode, AnnihilatePayload::decode, AnnihilatePayload::handle);
        add(4, SummonIronGolemPayload.class, SummonIronGolemPayload::encode, SummonIronGolemPayload::decode, SummonIronGolemPayload::handle);
        add(5, WarriorDuelPayload.class, WarriorDuelPayload::encode, WarriorDuelPayload::decode, WarriorDuelPayload::handle);
        add(6, AngelicFlightPayload.class, AngelicFlightPayload::encode, AngelicFlightPayload::decode, AngelicFlightPayload::handle);
        add(7, TemporalMarkPayload.class, TemporalMarkPayload::encode, TemporalMarkPayload::decode, TemporalMarkPayload::handle);
        add(8, TemporalRewindPayload.class, TemporalRewindPayload::encode, TemporalRewindPayload::decode, TemporalRewindPayload::handle);
        add(9, TimeWarpPayload.class, TimeWarpPayload::encode, TimeWarpPayload::decode, TimeWarpPayload::handle);
        add(10, DivineSanctifyPayload.class, DivineSanctifyPayload::encode, DivineSanctifyPayload::decode, DivineSanctifyPayload::handle);
        add(11, CursedLiberationPayload.class, CursedLiberationPayload::encode, CursedLiberationPayload::decode, CursedLiberationPayload::handle);
        add(12, AnnihilationVisualPayload.class, AnnihilationVisualPayload::encode, AnnihilationVisualPayload::decode, AnnihilationVisualPayload::handle);
        add(13, AngelicGuardEffectPayload.class, AngelicGuardEffectPayload::encode, AngelicGuardEffectPayload::decode, AngelicGuardEffectPayload::handle);
        add(14, NerfedReviveEffectPayload.class, NerfedReviveEffectPayload::encode, NerfedReviveEffectPayload::decode, NerfedReviveEffectPayload::handle);
    }

    private ModNetworking() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playBidirectional(CrownPayload.TYPE, CrownPayload.STREAM_CODEC, ModNetworking::handle);
    }

    private static <T> void add(int id, Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder,
                                Function<FriendlyByteBuf, T> decoder,
                                BiConsumer<T, Supplier<NetworkEvent.Context>> handler) {
        Registration<T> registration = new Registration<>(id, encoder, decoder, handler);
        BY_ID.put(id, registration);
        BY_CLASS.put(type, registration);
    }

    static void encode(FriendlyByteBuf buffer, Object message) {
        Registration<?> registration = BY_CLASS.get(message.getClass());
        if (registration == null) throw new IllegalArgumentException("Unregistered Crown payload: " + message.getClass());
        buffer.writeVarInt(registration.id());
        registration.encodeUnchecked(buffer, message);
    }

    static CrownPayload decode(FriendlyByteBuf buffer) {
        Registration<?> registration = BY_ID.get(buffer.readVarInt());
        if (registration == null) throw new IllegalArgumentException("Unknown Crown payload id");
        return new CrownPayload(registration.decoder().apply(buffer));
    }

    private static void handle(CrownPayload payload, IPayloadContext context) {
        Registration<?> registration = BY_CLASS.get(payload.message().getClass());
        if (registration == null) throw new IllegalArgumentException("Unregistered Crown payload: " + payload.message().getClass());
        registration.handleUnchecked(payload.message(), context);
    }

    public static final class Channel {
        public void sendToServer(Object message) {
            PacketDistributor.sendToServer(new CrownPayload(message));
        }

        public void send(Target target, Object message) {
            if (target.value() instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new CrownPayload(message));
                return;
            }
            if (target.value() instanceof ResourceKey<?> key) {
                @SuppressWarnings("unchecked") ResourceKey<Level> dimension = (ResourceKey<Level>) key;
                ServerLevel level = ServerLifecycleHooks.getCurrentServer().getLevel(dimension);
                if (level != null) PacketDistributor.sendToPlayersInDimension(level, new CrownPayload(message));
                return;
            }
            throw new IllegalArgumentException("Unsupported Crown packet target: " + target.value());
        }
    }

    private record Registration<T>(int id, BiConsumer<T, FriendlyByteBuf> encoder,
                                   Function<FriendlyByteBuf, T> decoder,
                                   BiConsumer<T, Supplier<NetworkEvent.Context>> handler) {
        @SuppressWarnings("unchecked")
        void encodeUnchecked(FriendlyByteBuf buffer, Object value) {
            encoder.accept((T) value, buffer);
        }

        @SuppressWarnings("unchecked")
        void handleUnchecked(Object value, IPayloadContext context) {
            handler.accept((T) value, () -> new NetworkEvent.Context(context));
        }
    }
}
