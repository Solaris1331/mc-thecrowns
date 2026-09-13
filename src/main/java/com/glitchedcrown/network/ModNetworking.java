package com.glitchedcrown.network;

import com.glitchedcrown.GlitchedCrownMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetworking {
    private static final String PROTOCOL = "12";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(GlitchedCrownMod.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private ModNetworking() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, FireLaserPayload.class,
                FireLaserPayload::encode, FireLaserPayload::decode, FireLaserPayload::handle);
        CHANNEL.registerMessage(id++, ToggleFatePayload.class,
                ToggleFatePayload::encode, ToggleFatePayload::decode, ToggleFatePayload::handle);
        CHANNEL.registerMessage(id++, ToggleAuraPayload.class,
                ToggleAuraPayload::encode, ToggleAuraPayload::decode, ToggleAuraPayload::handle);
        CHANNEL.registerMessage(id++, AnnihilatePayload.class,
                AnnihilatePayload::encode, AnnihilatePayload::decode, AnnihilatePayload::handle);
        CHANNEL.registerMessage(id++, SummonIronGolemPayload.class,
                SummonIronGolemPayload::encode, SummonIronGolemPayload::decode, SummonIronGolemPayload::handle);
        CHANNEL.registerMessage(id++, WarriorDuelPayload.class,
                WarriorDuelPayload::encode, WarriorDuelPayload::decode, WarriorDuelPayload::handle);
        CHANNEL.registerMessage(id++, AngelicFlightPayload.class,
                AngelicFlightPayload::encode, AngelicFlightPayload::decode, AngelicFlightPayload::handle);
        CHANNEL.registerMessage(id++, TemporalMarkPayload.class,
                TemporalMarkPayload::encode, TemporalMarkPayload::decode, TemporalMarkPayload::handle);
        CHANNEL.registerMessage(id++, TemporalRewindPayload.class,
                TemporalRewindPayload::encode, TemporalRewindPayload::decode, TemporalRewindPayload::handle);
        CHANNEL.registerMessage(id++, TimeWarpPayload.class,
                TimeWarpPayload::encode, TimeWarpPayload::decode, TimeWarpPayload::handle);
        CHANNEL.registerMessage(id++, DivineSanctifyPayload.class,
                DivineSanctifyPayload::encode, DivineSanctifyPayload::decode, DivineSanctifyPayload::handle);
        CHANNEL.registerMessage(id++, CursedLiberationPayload.class,
                CursedLiberationPayload::encode, CursedLiberationPayload::decode, CursedLiberationPayload::handle);
        CHANNEL.registerMessage(id++, AnnihilationVisualPayload.class,
                AnnihilationVisualPayload::encode, AnnihilationVisualPayload::decode,
                AnnihilationVisualPayload::handle, java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, AngelicGuardEffectPayload.class,
                AngelicGuardEffectPayload::encode, AngelicGuardEffectPayload::decode,
                AngelicGuardEffectPayload::handle, java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id, NerfedReviveEffectPayload.class,
                NerfedReviveEffectPayload::encode, NerfedReviveEffectPayload::decode,
                NerfedReviveEffectPayload::handle, java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
