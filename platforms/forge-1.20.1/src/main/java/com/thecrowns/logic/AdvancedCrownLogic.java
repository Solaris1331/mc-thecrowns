package com.thecrowns.logic;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.compat.FTBTeamsCompat;
import com.thecrowns.config.CrownServerConfig;
import com.thecrowns.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CuriosApi;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Mechanics for Crowns added after the original Tier I-III set. */
public final class AdvancedCrownLogic {
    private static final int CURSED_TRANSFORM_SCAN_INTERVAL = 10;
    private static final int TIME_WARP_SCAN_INTERVAL = 2;
    public static final int TIME_REWIND_COOLDOWN_TICKS = 300 * 20;
    public static final int TIME_WARP_COOLDOWN_TICKS = 180 * 20;
    public static final int TIME_WARP_DURATION_TICKS = 15 * 20;
    public static final int TIME_OUT_OF_COMBAT_TICKS = 10 * 20;
    public static final int FROST_DECAY_TICKS = 5 * 20;
    public static final int FROST_FREEZE_TICKS = 3 * 20;
    public static final int DIVINE_SANCTIFY_COOLDOWN_TICKS = 180 * 20;
    public static final int CURSED_LIBERATION_DURATION_TICKS = 60 * 20;
    public static final int CURSED_LIBERATION_COOLDOWN_TICKS = 600 * 20;

    public static final String TAG_TIME_REWIND_READY_AT = "TheCrownsTimeRewindReadyAt";
    public static final String TAG_TIME_WARP_READY_AT = "TheCrownsTimeWarpReadyAt";
    public static final String TAG_TIME_SNAPSHOT = "TheCrownsTimeSnapshot";
    public static final String TAG_DIVINE_READY_AT = "TheCrownsDivineReadyAt";
    public static final String TAG_CURSED_READY_AT = "TheCrownsCursedReadyAt";
    public static final String TAG_CURSED_BOUND = "TheCrownsCursedBound";
    private static final String TAG_DEATH_KEEP_ID = "TheCrownsDeathKeepId";

    private static final String NBT_TIME_REWIND_READY = "thecrowns_time_rewind_ready";
    private static final String NBT_TIME_WARP_READY = "thecrowns_time_warp_ready";
    private static final String NBT_TIME_WARP_UNTIL = "thecrowns_time_warp_until";
    private static final String NBT_TIME_WARP_NEXT_SCAN = "thecrowns_time_warp_next_scan";
    private static final String NBT_TIME_LAST_COMBAT = "thecrowns_time_last_combat";
    private static final String NBT_TIME_SNAPSHOT = "thecrowns_time_snapshot";
    private static final String NBT_TIME_REWIND_CAST_UNTIL = "thecrowns_time_rewind_cast_until";
    private static final String NBT_TIME_FLY_ACTIVE = "thecrowns_time_fly_active";
    private static final String NBT_TIME_BASE_FLY_SPEED = "thecrowns_time_base_fly_speed";
    private static final String NBT_TIME_KEEP_ITEMS = "thecrowns_time_keep_items";

    private static final String NBT_FROST_STACKS = "thecrowns_frost_stacks";
    private static final String NBT_FROST_NEXT_DECAY = "thecrowns_frost_next_decay";
    private static final String NBT_FROST_NEXT_STACK = "thecrowns_frost_next_stack";
    private static final String NBT_FROST_REFREEZE_READY = "thecrowns_frost_refreeze_ready";
    private static final String NBT_FROZEN_UNTIL = "thecrowns_frozen_until";
    private static final String NBT_FROZEN_X = "thecrowns_frozen_x";
    private static final String NBT_FROZEN_Y = "thecrowns_frozen_y";
    private static final String NBT_FROZEN_Z = "thecrowns_frozen_z";
    private static final String NBT_TIME_SLOWED_UNTIL = "thecrowns_time_slowed_until";
    private static final String NBT_TIME_STOPPED_UNTIL = "thecrowns_time_stopped_until";
    private static final String NBT_TIME_STOPPED_X = "thecrowns_time_stopped_x";
    private static final String NBT_TIME_STOPPED_Y = "thecrowns_time_stopped_y";
    private static final String NBT_TIME_STOPPED_Z = "thecrowns_time_stopped_z";

    private static final String NBT_DIVINE_READY = "thecrowns_divine_ready";
    private static final String NBT_DIVINE_PASSIVE_READY = "thecrowns_divine_passive_ready";
    private static final String NBT_CURSED_READY = "thecrowns_cursed_ready";
    private static final String NBT_CURSED_LIBERATION_UNTIL = "thecrowns_cursed_liberation_until";
    private static final String NBT_CURSE_TRANSFER_UNTIL = "thecrowns_curse_transfer_until";
    private static final String NBT_CURSED_BOUND_STACK = "thecrowns_cursed_bound_stack";
    private static final String NBT_CURSED_RESPAWN_PENDING = "thecrowns_cursed_respawn_pending";

    private static final UUID TIME_SPEED = uuid("temporal_speed");
    private static final UUID TIME_ATTACK_SPEED = uuid("temporal_attack_speed");
    private static final UUID TIME_WARP_MOVE = uuid("time_warp_move");
    private static final UUID TIME_WARP_ATTACK = uuid("time_warp_attack");
    private static final UUID FROST_DAMAGE = uuid("frost_damage");
    private static final UUID FROST_ARMOR = uuid("frost_armor");
    private static final UUID FROST_TOUGHNESS = uuid("frost_toughness");
    private static final UUID FROST_SLOW = uuid("frost_slow");
    private static final UUID DIVINE_HEALTH_FLAT = uuid("divine_health_flat");
    private static final UUID DIVINE_HEALTH_PERCENT = uuid("divine_health_percent");
    private static final UUID DIVINE_ARMOR = uuid("divine_armor");
    private static final UUID DIVINE_TOUGHNESS = uuid("divine_toughness");
    private static final UUID CURSED_HEALTH = uuid("cursed_health");
    private static final UUID CURSED_ARMOR = uuid("cursed_armor");
    private static final UUID CURSED_TOUGHNESS = uuid("cursed_toughness");
    private static final UUID CURSED_LUCK = uuid("cursed_luck");
    private static final UUID TRANSFER_HEALTH = uuid("curse_transfer_health");
    private static final UUID TRANSFER_ARMOR = uuid("curse_transfer_armor");
    private static final UUID TRANSFER_TOUGHNESS = uuid("curse_transfer_toughness");

    private static final ConcurrentHashMap<UUID, Set<UUID>> TIME_WARP_PROJECTILES = new ConcurrentHashMap<>();

    private AdvancedCrownLogic() {}

    public static int temporalRewindCooldownTicks() { return CrownServerConfig.TEMPORAL_REWIND_COOLDOWN_SECONDS.get() * 20; }
    public static int temporalRewindConcentrationTicks() { return CrownServerConfig.TEMPORAL_REWIND_CONCENTRATION_SECONDS.get() * 20; }
    public static int temporalWarpCooldownTicks() { return CrownServerConfig.TEMPORAL_WARP_COOLDOWN_SECONDS.get() * 20; }
    public static int temporalWarpDurationTicks() { return CrownServerConfig.TEMPORAL_WARP_DURATION_SECONDS.get() * 20; }
    public static int temporalOutOfCombatTicks() { return CrownServerConfig.TEMPORAL_OUT_OF_COMBAT_SECONDS.get() * 20; }
    public static int frostDecayTicks() { return CrownServerConfig.FROST_DECAY_SECONDS.get() * 20; }
    public static int frostFreezeTicks(LivingEntity target) {
        return (target instanceof Player || CrownLogic.isBoss(target)
                ? CrownServerConfig.FROST_BOSS_PLAYER_FREEZE_SECONDS.get()
                : CrownServerConfig.FROST_FREEZE_SECONDS.get()) * 20;
    }
    public static int frostStackInternalCooldownTicks() { return secondsToTicks(CrownServerConfig.FROST_STACK_INTERNAL_COOLDOWN_SECONDS.get()); }
    public static int frostRefreezeCooldownTicks(LivingEntity target) {
        int seconds = target instanceof Player || CrownLogic.isBoss(target)
                ? CrownServerConfig.FROST_BOSS_PLAYER_REFREEZE_COOLDOWN_SECONDS.get()
                : CrownServerConfig.FROST_REFREEZE_COOLDOWN_SECONDS.get();
        return seconds * 20;
    }
    public static int divineNegativeEffectCooldownTicks() { return secondsToTicks(CrownServerConfig.DIVINE_NEGATIVE_EFFECT_COOLDOWN_SECONDS.get()); }
    public static int divineSanctifyCooldownTicks() { return CrownServerConfig.DIVINE_SANCTIFY_COOLDOWN_SECONDS.get() * 20; }
    public static int cursedLiberationDurationTicks() { return CrownServerConfig.CURSED_LIBERATION_DURATION_SECONDS.get() * 20; }
    public static int cursedLiberationCooldownTicks() { return CrownServerConfig.CURSED_LIBERATION_COOLDOWN_SECONDS.get() * 20; }

    private static int secondsToTicks(double seconds) {
        return Math.max(0, (int) Math.min(Integer.MAX_VALUE, Math.round(seconds * 20.0D)));
    }

    private static UUID uuid(String path) {
        return UUID.nameUUIDFromBytes((TheCrownsMod.MOD_ID + ":" + path).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isWearingTemporal(LivingEntity entity) { return NewCrownLogic.isWearing(entity, ModItems.TEMPORAL_CROWN.get()); }
    public static boolean isWearingFrost(LivingEntity entity) { return NewCrownLogic.isWearing(entity, ModItems.FROST_CROWN.get()); }
    public static boolean isWearingDivine(LivingEntity entity) { return NewCrownLogic.isWearing(entity, ModItems.DIVINE_CROWN.get()); }
    public static boolean isWearingCursed(LivingEntity entity) { return NewCrownLogic.isWearing(entity, ModItems.CURSED_CROWN.get()); }
    public static boolean hasCursedPower(LivingEntity entity) {
        return isWearingCursed(entity) || GlitchedFusionLogic.has(entity, ModItems.CURSED_CROWN.get());
    }

    private static boolean hasFrostStackPower(LivingEntity entity) {
        return isWearingFrost(entity) || GlitchedFusionLogic.has(entity, ModItems.FROST_CROWN.get());
    }

    /**
     * Cursed Crown follows a Binding-Curse-style removal lock while its wearer is alive.
     * Creative players, or wearers who also have a Glitched / Unleashed / U^2 Crown equipped,
     * are explicitly allowed to remove it. Death is handled separately by the bound-stack
     * persistence path so the Crown remains with the player across death.
     */
    public static boolean canRemoveCursedCrown(LivingEntity wearer) {
        if (wearer == null || !wearer.isAlive()) return true;
        if (wearer instanceof Player player && player.isCreative()) return true;
        return CrownLogic.isWearingGlitched(wearer) || CrownLogic.isWearingUnleashed(wearer);
    }

    /** True when the player owns a Cursed Crown in vanilla inventory/equipment or any Curios slot. */
    public static boolean hasCursedCrown(ServerPlayer player) {
        if (player == null) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModItems.CURSED_CROWN.get())) return true;
        }
        try {
            return CuriosApi.getCuriosInventory(player).resolve().map(inv -> {
                for (var curio : inv.getCurios().values()) {
                    var stacks = curio.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        if (stacks.getStackInSlot(i).is(ModItems.CURSED_CROWN.get())) return true;
                    }
                }
                return false;
            }).orElse(false);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static void copyPersistentPlayerData(Player original, Player replacement) {
        CompoundTag src = original.getPersistentData();
        CompoundTag dst = replacement.getPersistentData();
        for (String key : new String[]{NBT_TIME_REWIND_READY, NBT_TIME_WARP_READY, NBT_TIME_LAST_COMBAT,
                NBT_TIME_SNAPSHOT, NBT_DIVINE_READY, NBT_CURSED_READY, NBT_CURSED_LIBERATION_UNTIL,
                NBT_TIME_KEEP_ITEMS, NBT_CURSED_BOUND_STACK, NBT_CURSED_RESPAWN_PENDING}) {
            if (src.contains(key)) dst.put(key, src.get(key).copy());
        }
    }

    public static void tickPlayer(ServerPlayer player) {
        MixinDiagnostics.auditAfterPlayerJoin(player);
        if (player.tickCount % CURSED_TRANSFORM_SCAN_INTERVAL == 0) transformCursedCrowns(player);
        maintainCursedBinding(player);
        restoreDeathKeptCrowns(player);
        syncPlayerAttributes(player);
        if (isWearingFrost(player)) {
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            player.removeEffect(MobEffects.DIG_SLOWDOWN);
        }
        tickTemporalFlightSpeed(player);
        tickTemporalRewindConcentration(player);
        tickTemporalCooldownAcceleration(player);
        tickTimeWarp(player);
        tickPowderSnow(player);
        tickFrostAffinity(player);
        syncTooltipState(player);
    }

    public static void tickLiving(LivingEntity entity) {
        if (entity.level().isClientSide) return;
        CompoundTag data = entity.getPersistentData();
        // LivingTickEvent is delivered for every loaded creature. Entities without one of
        // these temporary Crown states have no modifier or NBT cleanup to perform.
        if (!hasLivingRuntimeState(data)) return;
        long now = entity.level().getGameTime();

        tickTimeStopState(entity, now);

        long slowedUntil = data.getLong(NBT_TIME_SLOWED_UNTIL);
        boolean slowed = slowedUntil > now;
        double warpSlow = CrownServerConfig.TEMPORAL_WARP_SLOW_FRACTION.get();
        setModifier(entity, Attributes.MOVEMENT_SPEED, TIME_WARP_MOVE, "time_warp_move", slowed ? -warpSlow : 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setModifier(entity, Attributes.ATTACK_SPEED, TIME_WARP_ATTACK, "time_warp_attack", slowed ? -warpSlow : 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (!slowed && data.contains(NBT_TIME_SLOWED_UNTIL)) data.remove(NBT_TIME_SLOWED_UNTIL);

        long curseUntil = data.getLong(NBT_CURSE_TRANSFER_UNTIL);
        boolean transferredCurse = curseUntil > now;
        syncTransferredCurse(entity, transferredCurse);
        if (!transferredCurse && data.contains(NBT_CURSE_TRANSFER_UNTIL)) data.remove(NBT_CURSE_TRANSFER_UNTIL);

        tickFrostState(entity, now);
    }

    private static boolean hasLivingRuntimeState(CompoundTag data) {
        return data.contains(NBT_TIME_STOPPED_UNTIL)
                || data.contains(NBT_TIME_SLOWED_UNTIL)
                || data.contains(NBT_CURSE_TRANSFER_UNTIL)
                || data.contains(NBT_FROZEN_UNTIL)
                || data.contains(NBT_FROST_STACKS);
    }

    public static boolean isTimeStopped(Entity entity) {
        return entity instanceof LivingEntity living
                && living.getPersistentData().getLong(NBT_TIME_STOPPED_UNTIL) > living.level().getGameTime();
    }

    public static boolean shouldCancelTimeStoppedAttack(net.minecraft.world.damagesource.DamageSource source) {
        if (source == null) return false;
        Entity owner = source.getEntity();
        return owner instanceof LivingEntity living && isTimeStopped(living);
    }

    private static void tickTimeStopState(LivingEntity entity, long now) {
        CompoundTag data = entity.getPersistentData();
        long stoppedUntil = data.getLong(NBT_TIME_STOPPED_UNTIL);
        if (stoppedUntil > now) {
            if (!data.contains(NBT_TIME_STOPPED_X)) {
                data.putDouble(NBT_TIME_STOPPED_X, entity.getX());
                data.putDouble(NBT_TIME_STOPPED_Y, entity.getY());
                data.putDouble(NBT_TIME_STOPPED_Z, entity.getZ());
            }
            entity.setDeltaMovement(Vec3.ZERO);
            entity.fallDistance = 0.0F;
            entity.setPos(data.getDouble(NBT_TIME_STOPPED_X), data.getDouble(NBT_TIME_STOPPED_Y), data.getDouble(NBT_TIME_STOPPED_Z));
            if (entity instanceof Mob mob) mob.getNavigation().stop();
            return;
        }
        if (data.contains(NBT_TIME_STOPPED_UNTIL)) {
            data.remove(NBT_TIME_STOPPED_UNTIL);
            data.remove(NBT_TIME_STOPPED_X);
            data.remove(NBT_TIME_STOPPED_Y);
            data.remove(NBT_TIME_STOPPED_Z);
        }
    }

    private static void syncPlayerAttributes(ServerPlayer player) {
        boolean temporal = isWearingTemporal(player);
        boolean frost = isWearingFrost(player);
        boolean divine = isWearingDivine(player);
        boolean cursedPenalty = isOriginalCursedPenaltyActive(player);

        setModifier(player, Attributes.MOVEMENT_SPEED, TIME_SPEED, "temporal_crown_speed",
                temporal ? CrownServerConfig.TEMPORAL_MOVEMENT_SPEED_BONUS.get() : 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setModifier(player, Attributes.ATTACK_SPEED, TIME_ATTACK_SPEED, "temporal_crown_attack_speed",
                temporal ? CrownServerConfig.TEMPORAL_ATTACK_SPEED_BONUS.get() : 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);

        setModifier(player, Attributes.ATTACK_DAMAGE, FROST_DAMAGE, "frost_crown_damage",
                frost ? CrownServerConfig.FROST_ATTACK_DAMAGE.get() : 0.0D, AttributeModifier.Operation.ADDITION);
        setModifier(player, Attributes.ARMOR, FROST_ARMOR, "frost_crown_armor",
                frost ? CrownServerConfig.FROST_ARMOR.get() : 0.0D, AttributeModifier.Operation.ADDITION);
        setModifier(player, Attributes.ARMOR_TOUGHNESS, FROST_TOUGHNESS, "frost_crown_toughness",
                frost ? CrownServerConfig.FROST_TOUGHNESS.get() : 0.0D, AttributeModifier.Operation.ADDITION);

        setModifier(player, Attributes.MAX_HEALTH, DIVINE_HEALTH_FLAT, "divine_crown_health_flat",
                divine ? CrownServerConfig.DIVINE_FLAT_MAX_HEALTH.get() : 0.0D, AttributeModifier.Operation.ADDITION);
        setModifier(player, Attributes.MAX_HEALTH, DIVINE_HEALTH_PERCENT, "divine_crown_health_percent",
                divine ? CrownServerConfig.DIVINE_PERCENT_MAX_HEALTH.get() : 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setModifier(player, Attributes.ARMOR, DIVINE_ARMOR, "divine_crown_armor",
                divine ? CrownServerConfig.DIVINE_ARMOR.get() : 0.0D, AttributeModifier.Operation.ADDITION);
        setModifier(player, Attributes.ARMOR_TOUGHNESS, DIVINE_TOUGHNESS, "divine_crown_toughness",
                divine ? CrownServerConfig.DIVINE_TOUGHNESS.get() : 0.0D, AttributeModifier.Operation.ADDITION);

        double cursedFraction = CrownServerConfig.CURSED_PENALTY_FRACTION.get();
        setModifier(player, Attributes.MAX_HEALTH, CURSED_HEALTH, "cursed_crown_health", cursedPenalty ? -cursedFraction : 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setModifier(player, Attributes.ARMOR, CURSED_ARMOR, "cursed_crown_armor", cursedPenalty ? -cursedFraction : 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setModifier(player, Attributes.ARMOR_TOUGHNESS, CURSED_TOUGHNESS, "cursed_crown_toughness", cursedPenalty ? -cursedFraction : 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setModifier(player, Attributes.LUCK, CURSED_LUCK, "cursed_crown_luck",
                isWearingCursed(player) ? CrownServerConfig.CURSED_LUCK_BONUS.get() : 0.0D, AttributeModifier.Operation.ADDITION);

        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static void syncTransferredCurse(LivingEntity entity, boolean active) {
        double fraction = CrownServerConfig.CURSED_PENALTY_FRACTION.get();
        setModifier(entity, Attributes.MAX_HEALTH, TRANSFER_HEALTH, "curse_transfer_health", active ? -fraction : 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setModifier(entity, Attributes.ARMOR, TRANSFER_ARMOR, "curse_transfer_armor", active ? -fraction : 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setModifier(entity, Attributes.ARMOR_TOUGHNESS, TRANSFER_TOUGHNESS, "curse_transfer_toughness", active ? -fraction : 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (entity.getHealth() > entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
    }

    private static void setModifier(LivingEntity entity, Attribute attribute, UUID id, String name,
                                    double amount, AttributeModifier.Operation op) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier current = instance.getModifier(id);
        if (amount == 0.0D) {
            if (current != null) instance.removeModifier(id);
            return;
        }
        if (current != null && current.getAmount() == amount && current.getOperation() == op) return;
        if (current != null) instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, name, amount, op));
    }

    private static void tickTemporalFlightSpeed(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        boolean wearing = isWearingTemporal(player);
        boolean active = data.getBoolean(NBT_TIME_FLY_ACTIVE);
        if (wearing && !active) {
            float base = player.getAbilities().getFlyingSpeed();
            data.putFloat(NBT_TIME_BASE_FLY_SPEED, base);
            data.putBoolean(NBT_TIME_FLY_ACTIVE, true);
            player.getAbilities().setFlyingSpeed((float) (base * (1.0D + CrownServerConfig.TEMPORAL_FLIGHT_SPEED_BONUS.get())));
            player.onUpdateAbilities();
        } else if (wearing) {
            float base = data.getFloat(NBT_TIME_BASE_FLY_SPEED);
            float expected = (float) (base * (1.0D + CrownServerConfig.TEMPORAL_FLIGHT_SPEED_BONUS.get()));
            if (Math.abs(player.getAbilities().getFlyingSpeed() - expected) > 0.000001F) {
                player.getAbilities().setFlyingSpeed(expected);
                player.onUpdateAbilities();
            }
        } else if (!wearing && active) {
            player.getAbilities().setFlyingSpeed(data.getFloat(NBT_TIME_BASE_FLY_SPEED));
            data.remove(NBT_TIME_BASE_FLY_SPEED);
            data.remove(NBT_TIME_FLY_ACTIVE);
            player.onUpdateAbilities();
        }
    }

    private static void tickTemporalCooldownAcceleration(ServerPlayer player) {
        if (!isWearingTemporal(player)) return;
        long now = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        long lastCombat = data.getLong(NBT_TIME_LAST_COMBAT);
        if (lastCombat <= 0L) {
            data.putLong(NBT_TIME_LAST_COMBAT, now);
            return;
        }
        if (now - lastCombat < temporalOutOfCombatTicks()) return;
        accelerateReadyAt(data, NBT_TIME_REWIND_READY, now);
        accelerateReadyAt(data, NBT_TIME_WARP_READY, now);
    }

    public static boolean isTemporalOutOfCombat(ServerPlayer player) {
        if (player == null) return false;
        long now = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        long lastCombat = data.getLong(NBT_TIME_LAST_COMBAT);
        if (lastCombat <= 0L) {
            data.putLong(NBT_TIME_LAST_COMBAT, now);
            return false;
        }
        return now - lastCombat >= temporalOutOfCombatTicks();
    }

    private static void accelerateReadyAt(CompoundTag data, String key, long now) {
        long ready = data.getLong(key);
        if (ready > now) data.putLong(key, ready - 1L);
    }

    public static void noteCombat(ServerPlayer player) {
        if (player != null) {
            player.getPersistentData().putLong(NBT_TIME_LAST_COMBAT, player.level().getGameTime());
        }
    }

    public static boolean isTemporalEffectImmune(LivingEntity entity, MobEffect effect) {
        if (!isWearingTemporal(entity) || effect == null) return false;
        if (effect == MobEffects.MOVEMENT_SLOWDOWN || effect == MobEffects.DIG_SLOWDOWN) return true;
        try {
            for (var entry : effect.getAttributeModifiers().entrySet()) {
                if ((entry.getKey() == Attributes.MOVEMENT_SPEED || entry.getKey() == Attributes.ATTACK_SPEED)
                        && entry.getValue().getAmount() < 0.0D) return true;
            }
        } catch (RuntimeException ignored) {}
        return false;
    }

    public static boolean isFrostDamageImmune(LivingEntity entity, net.minecraft.world.damagesource.DamageSource source) {
        return isWearingFrost(entity) && source != null && source.is(net.minecraft.world.damagesource.DamageTypes.FREEZE);
    }

    private static void tickPowderSnow(ServerPlayer player) {
        if (!isWearingFrost(player) || !player.isInPowderSnow) return;
        Vec3 motion = player.getDeltaMovement();
        if (motion.y < 0.0D) player.setDeltaMovement(motion.x, 0.0D, motion.z);
        player.fallDistance = 0.0F;
        player.setOnGround(true);
    }

    private static void tickFrostAffinity(ServerPlayer player) {
        if (player.tickCount % 10 != 0 || !hasFrostStackPower(player)
                || !(player.level() instanceof ServerLevel level)) return;
        double radius = CrownServerConfig.FROST_AFFINITY_RADIUS.get();
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(radius),
                m -> isFrostFriendlyMob(m) && m.getTarget() == player)) {
            mob.setTarget(null);
            mob.getNavigation().stop();
        }
    }

    public static boolean shouldBlockFrostTarget(Mob mob, ServerPlayer player) {
        return hasFrostStackPower(player) && isFrostFriendlyMob(mob);
    }

    private static boolean isFrostFriendlyMob(Mob mob) {
        if (mob instanceof PolarBear || mob instanceof Stray) return true;
        if (!(mob instanceof Drowned)) return false;
        return mob.level().getBiome(mob.blockPosition()).value().getBaseTemperature()
                <= CrownServerConfig.FROST_COLD_BIOME_TEMPERATURE.get();
    }

    public static boolean isFrozen(LivingEntity entity) {
        return entity != null && entity.getPersistentData().getLong(NBT_FROZEN_UNTIL) > entity.level().getGameTime();
    }

    public static boolean shouldCancelFrozenAttack(net.minecraft.world.damagesource.DamageSource source) {
        if (source == null) return false;
        Entity owner = source.getEntity();
        return owner instanceof LivingEntity living && isFrozen(living);
    }

    private static void tickFrostState(LivingEntity entity, long now) {
        CompoundTag data = entity.getPersistentData();
        long frozenUntil = data.getLong(NBT_FROZEN_UNTIL);
        if (frozenUntil > now) {
            setModifier(entity, Attributes.MOVEMENT_SPEED, FROST_SLOW, "frost_stack_slow", 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            if (!data.contains(NBT_FROZEN_X)) {
                data.putDouble(NBT_FROZEN_X, entity.getX());
                data.putDouble(NBT_FROZEN_Y, entity.getY());
                data.putDouble(NBT_FROZEN_Z, entity.getZ());
            }
            double x = data.getDouble(NBT_FROZEN_X), y = data.getDouble(NBT_FROZEN_Y), z = data.getDouble(NBT_FROZEN_Z);
            entity.setDeltaMovement(Vec3.ZERO);
            entity.fallDistance = 0.0F;
            entity.setPos(x, y, z);
            if (entity instanceof Mob mob) mob.getNavigation().stop();
            if (entity.level() instanceof ServerLevel level && entity.tickCount % 4 == 0) {
                level.sendParticles(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY() + entity.getBbHeight() * 0.55D, entity.getZ(),
                        8, entity.getBbWidth() * 0.55D, entity.getBbHeight() * 0.45D, entity.getBbWidth() * 0.55D, 0.01D);
            }
            return;
        }
        if (data.contains(NBT_FROZEN_UNTIL)) {
            data.remove(NBT_FROZEN_UNTIL);
            data.remove(NBT_FROZEN_X); data.remove(NBT_FROZEN_Y); data.remove(NBT_FROZEN_Z);
        }

        int freezeThreshold = CrownServerConfig.FROST_STACKS_TO_FREEZE.get();
        int stacks = Math.max(0, Math.min(freezeThreshold, data.getInt(NBT_FROST_STACKS)));
        if (stacks > 0) {
            long next = data.getLong(NBT_FROST_NEXT_DECAY);
            if (next <= 0L) {
                data.putLong(NBT_FROST_NEXT_DECAY, now + frostDecayTicks());
            } else if (now >= next) {
                stacks--;
                data.putInt(NBT_FROST_STACKS, stacks);
                if (stacks > 0) data.putLong(NBT_FROST_NEXT_DECAY, next + frostDecayTicks());
                else data.remove(NBT_FROST_NEXT_DECAY);
            }
        } else {
            data.remove(NBT_FROST_STACKS);
            data.remove(NBT_FROST_NEXT_DECAY);
        }
        setModifier(entity, Attributes.MOVEMENT_SPEED, FROST_SLOW, "frost_stack_slow",
                stacks > 0 ? -Math.min(1.0D, CrownServerConfig.FROST_SLOW_PER_STACK.get() * stacks) : 0.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public static void applyFrostStack(LivingEntity target) {
        if (target == null || target.level().isClientSide || isFrozen(target)) return;
        long now = target.level().getGameTime();
        CompoundTag data = target.getPersistentData();
        if (now < data.getLong(NBT_FROST_NEXT_STACK)) return;
        data.putLong(NBT_FROST_NEXT_STACK, now + frostStackInternalCooldownTicks());
        int threshold = CrownServerConfig.FROST_STACKS_TO_FREEZE.get();
        int stacks = Math.min(threshold, Math.max(0, data.getInt(NBT_FROST_STACKS)) + 1);
        if (stacks >= threshold && now >= data.getLong(NBT_FROST_REFREEZE_READY)) {
            data.putInt(NBT_FROST_STACKS, 0);
            data.remove(NBT_FROST_NEXT_DECAY);
            data.putLong(NBT_FROZEN_UNTIL, now + frostFreezeTicks(target));
            data.putLong(NBT_FROST_REFREEZE_READY, now + frostRefreezeCooldownTicks(target));
            data.putDouble(NBT_FROZEN_X, target.getX());
            data.putDouble(NBT_FROZEN_Y, target.getY());
            data.putDouble(NBT_FROZEN_Z, target.getZ());
            if (target.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                        30, 0.5D, 0.6D, 0.5D, 0.03D);
                level.playSound(null, target.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 0.8F, 1.6F);
            }
        } else {
            data.putInt(NBT_FROST_STACKS, stacks);
            if (!data.contains(NBT_FROST_NEXT_DECAY)) data.putLong(NBT_FROST_NEXT_DECAY, now + frostDecayTicks());
        }
    }

    public static void onDamageResolved(LivingEntity target, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (target == null || source == null || amount <= 0.0F) return;
        ServerPlayer attacker = NewCrownLogic.damageOwner(source);
        if (attacker != null) {
            cancelTemporalRewindConcentration(attacker);
            noteCombat(attacker);
            if (hasFrostStackPower(attacker) && target != attacker) applyFrostStack(target);
        }
        if (target instanceof ServerPlayer victim) {
            cancelTemporalRewindConcentration(victim);
            noteCombat(victim);
            Entity responsible = source.getEntity();
            if (!(responsible instanceof LivingEntity)) responsible = source.getDirectEntity();
            if (hasFrostStackPower(victim) && responsible instanceof LivingEntity living && living != victim) applyFrostStack(living);
        }
    }

    /** Adds the Frost Crown's configured cold component while retaining the original damage owner/source. */
    public static float addFrostDamage(net.minecraft.world.damagesource.DamageSource source, float amount) {
        ServerPlayer attacker = NewCrownLogic.damageOwner(source);
        if (attacker != null && isWearingFrost(attacker) && amount > 0.0F) {
            return NewCrownLogic.sanitizeCrownDamage(amount + CrownServerConfig.FROST_BONUS_DAMAGE.get().floatValue());
        }
        return amount;
    }

    public static float applyCursedOutgoingPenalty(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (amount <= 0.0F || source == null) return amount;
        Entity owner = source.getEntity();
        if (!(owner instanceof LivingEntity living)) return amount;
        return applyCursedOutgoingPenalty(living, amount);
    }

    public static float applyCursedOutgoingPenalty(LivingEntity owner, float amount) {
        if (amount > 0.0F && isCursedPenaltyActive(owner)) {
            return NewCrownLogic.sanitizeCrownDamage(amount
                    * (1.0F - CrownServerConfig.CURSED_PENALTY_FRACTION.get().floatValue()));
        }
        return amount;
    }

    public static boolean isCursedPenaltyActive(LivingEntity entity) {
        if (entity == null) return false;
        long now = entity.level().getGameTime();
        if (entity.getPersistentData().getLong(NBT_CURSE_TRANSFER_UNTIL) > now) return true;
        return entity instanceof ServerPlayer player && isOwnCursedPenaltyActive(player);
    }

    public static boolean isOwnCursedPenaltyActive(ServerPlayer player) {
        if (!hasCursedPower(player)) return false;
        return player.getPersistentData().getLong(NBT_CURSED_LIBERATION_UNTIL) <= player.level().getGameTime();
    }

    private static boolean isOriginalCursedPenaltyActive(ServerPlayer player) {
        return isWearingCursed(player)
                && player.getPersistentData().getLong(NBT_CURSED_LIBERATION_UNTIL) <= player.level().getGameTime();
    }

    public static float modifyHealing(LivingEntity entity, float amount) {
        if (amount > 0.0F && isWearingDivine(entity)) {
            amount *= CrownServerConfig.DIVINE_HEALING_MULTIPLIER.get().floatValue();
        }
        return CrownLogic.clampHealingAmount(entity, amount);
    }

    public static void onNegativeEffectAdded(LivingEntity entity, MobEffectInstance negative) {
        if (!(entity instanceof ServerPlayer player) || negative == null || !isWearingDivine(player)) return;
        if (negative.getEffect().getCategory() != MobEffectCategory.HARMFUL) return;
        long now = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        if (now < data.getLong(NBT_DIVINE_PASSIVE_READY)) return;
        data.putLong(NBT_DIVINE_PASSIVE_READY, now + divineNegativeEffectCooldownTicks());
        player.heal(CrownServerConfig.DIVINE_NEGATIVE_EFFECT_BASE_HEAL.get().floatValue()
                + player.getMaxHealth() * CrownServerConfig.DIVINE_NEGATIVE_EFFECT_MAX_HEALTH_FRACTION.get().floatValue());
        MobEffect[] pool = {
                MobEffects.DAMAGE_BOOST, MobEffects.REGENERATION, MobEffects.HEALTH_BOOST, MobEffects.ABSORPTION,
                MobEffects.DAMAGE_RESISTANCE, MobEffects.FIRE_RESISTANCE, MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED,
                MobEffects.LUCK, MobEffects.NIGHT_VISION, MobEffects.WATER_BREATHING
        };
        MobEffect chosen = pool[player.getRandom().nextInt(pool.length)];
        int duration = Math.max(1, (int) Math.round(negative.getDuration()
                * CrownServerConfig.DIVINE_POSITIVE_EFFECT_DURATION_FRACTION.get()));
        player.addEffect(new MobEffectInstance(chosen, duration, negative.getAmplifier(), false, true, true));
    }

    public static boolean isConsumableSpeedBoosted(ServerPlayer player, ItemStack stack) {
        if (!isWearingTemporal(player) || stack == null || stack.isEmpty()) return false;
        UseAnim animation = stack.getUseAnimation();
        return animation == UseAnim.EAT || animation == UseAnim.DRINK;
    }

    public static int acceleratedUseDuration(int duration) {
        return Math.max(1, (int) Math.ceil(duration / CrownServerConfig.TEMPORAL_CONSUMABLE_SPEED_MULTIPLIER.get()));
    }

    public static void markTemporalPosition(ServerPlayer player) {
        if (!isWearingTemporal(player)) return;
        CompoundTag snap = new CompoundTag();
        snap.putString("dimension", player.level().dimension().location().toString());
        snap.putDouble("x", player.getX()); snap.putDouble("y", player.getY()); snap.putDouble("z", player.getZ());
        snap.putFloat("yaw", player.getYRot()); snap.putFloat("pitch", player.getXRot());
        snap.putFloat("health", player.getHealth());
        snap.putInt("food", player.getFoodData().getFoodLevel());
        snap.putFloat("saturation", player.getFoodData().getSaturationLevel());
        snap.putInt("totalXp", player.totalExperience);
        snap.putInt("level", player.experienceLevel);
        snap.putFloat("progress", player.experienceProgress);
        ListTag effects = new ListTag();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (effect.getEffect().getCategory() != MobEffectCategory.BENEFICIAL) continue;
            CompoundTag tag = new CompoundTag();
            effect.save(tag);
            effects.add(tag);
        }
        snap.put("effects", effects);
        player.getPersistentData().put(NBT_TIME_SNAPSHOT, snap);
        player.displayClientMessage(Component.translatable("message.thecrowns.temporal_marked"), true);
    }

    public static void rewindTemporal(ServerPlayer player) {
        if (!isWearingTemporal(player)) return;
        long now = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        if (now < data.getLong(NBT_TIME_REWIND_READY)
                || data.getLong(NBT_TIME_REWIND_CAST_UNTIL) > now
                || !data.contains(NBT_TIME_SNAPSHOT, Tag.TAG_COMPOUND)) return;
        data.putLong(NBT_TIME_REWIND_READY, now + temporalRewindCooldownTicks());
        int concentrationTicks = temporalRewindConcentrationTicks();
        if (concentrationTicks <= 0) {
            completeTemporalRewind(player);
            return;
        }
        data.putLong(NBT_TIME_REWIND_CAST_UNTIL, now + concentrationTicks);
        player.displayClientMessage(Component.translatable("message.thecrowns.temporal_rewind_concentrating",
                CrownServerConfig.TEMPORAL_REWIND_CONCENTRATION_SECONDS.get()), true);
        syncTooltipState(player);
    }

    private static void tickTemporalRewindConcentration(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        long until = data.getLong(NBT_TIME_REWIND_CAST_UNTIL);
        if (until <= 0L) return;
        if (!isWearingTemporal(player)) {
            cancelTemporalRewindConcentration(player);
            return;
        }
        if (player.level().getGameTime() >= until) completeTemporalRewind(player);
    }

    public static boolean cancelTemporalRewindConcentration(ServerPlayer player) {
        if (player == null) return false;
        CompoundTag data = player.getPersistentData();
        if (data.getLong(NBT_TIME_REWIND_CAST_UNTIL) <= player.level().getGameTime()) return false;
        data.remove(NBT_TIME_REWIND_CAST_UNTIL);
        player.displayClientMessage(Component.translatable("message.thecrowns.temporal_rewind_interrupted"), true);
        syncTooltipState(player);
        return true;
    }

    public static void clearTemporalSnapshot(Player player) {
        if (player == null) return;
        CompoundTag data = player.getPersistentData();
        data.remove(NBT_TIME_SNAPSHOT);
        data.remove(NBT_TIME_REWIND_CAST_UNTIL);
        if (player instanceof ServerPlayer serverPlayer) syncTooltipState(serverPlayer);
    }

    private static void completeTemporalRewind(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.remove(NBT_TIME_REWIND_CAST_UNTIL);
        if (!isWearingTemporal(player) || !data.contains(NBT_TIME_SNAPSHOT, Tag.TAG_COMPOUND)) return;
        CompoundTag snap = data.getCompound(NBT_TIME_SNAPSHOT);
        float currentHealth = player.getHealth();
        int currentFood = player.getFoodData().getFoodLevel();
        float currentSaturation = player.getFoodData().getSaturationLevel();
        int currentXp = player.totalExperience;

        ServerLevel destination = player.serverLevel();
        try {
            ResourceLocation dimId = new ResourceLocation(snap.getString("dimension"));
            ResourceKey<net.minecraft.world.level.Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimId);
            ServerLevel level = player.server.getLevel(dimKey);
            if (level != null) destination = level;
        } catch (RuntimeException ignored) {}
        player.teleportTo(destination, snap.getDouble("x"), snap.getDouble("y"), snap.getDouble("z"), snap.getFloat("yaw"), snap.getFloat("pitch"));
        player.fallDistance = 0.0F;

        player.setHealth(Math.min(player.getMaxHealth(), Math.max(currentHealth, snap.getFloat("health"))));
        player.getFoodData().setFoodLevel(Math.max(currentFood, snap.getInt("food")));
        player.getFoodData().setSaturation(Math.max(currentSaturation, snap.getFloat("saturation")));
        if (snap.getInt("totalXp") > currentXp) {
            player.totalExperience = snap.getInt("totalXp");
            player.experienceLevel = snap.getInt("level");
            player.experienceProgress = snap.getFloat("progress");
        }
        if (snap.contains("effects", Tag.TAG_LIST)) {
            ListTag list = snap.getList("effects", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                MobEffectInstance stored = MobEffectInstance.load(list.getCompound(i));
                if (stored == null || stored.getEffect().getCategory() != MobEffectCategory.BENEFICIAL) continue;
                MobEffectInstance current = player.getEffect(stored.getEffect());
                if (current == null || stored.getAmplifier() > current.getAmplifier()
                        || (stored.getAmplifier() == current.getAmplifier() && stored.getDuration() > current.getDuration())) {
                    player.addEffect(new MobEffectInstance(stored));
                }
            }
        }
        ItemStack crown = findEquipped(player, ModItems.TEMPORAL_CROWN.get());
        if (!crown.isEmpty()) crown.setDamageValue(0);
        player.level().playSound(null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0F, 1.4F);
        player.displayClientMessage(Component.translatable("message.thecrowns.temporal_rewind_complete"), true);
        syncTooltipState(player);
    }

    public static void activateTimeWarp(ServerPlayer player) {
        if (!isWearingTemporal(player)) return;
        long now = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        if (now < data.getLong(NBT_TIME_WARP_READY)) return;
        data.putLong(NBT_TIME_WARP_READY, now + temporalWarpCooldownTicks());
        data.putLong(NBT_TIME_WARP_UNTIL, now + temporalWarpDurationTicks());
        data.putLong(NBT_TIME_WARP_NEXT_SCAN, now);
        TIME_WARP_PROJECTILES.put(player.getUUID(), ConcurrentHashMap.newKeySet());
        syncTooltipState(player);
    }

    private static void tickTimeWarp(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        long now = player.level().getGameTime();
        long until = data.getLong(NBT_TIME_WARP_UNTIL);
        if (until <= now) {
            if (data.contains(NBT_TIME_WARP_UNTIL)) {
                removeTimeWarpProjectiles(player);
                data.remove(NBT_TIME_WARP_UNTIL);
                data.remove(NBT_TIME_WARP_NEXT_SCAN);
            }
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) return;
        if (now < data.getLong(NBT_TIME_WARP_NEXT_SCAN)) return;
        data.putLong(NBT_TIME_WARP_NEXT_SCAN, now + TIME_WARP_SCAN_INTERVAL);
        double radius = CrownServerConfig.TEMPORAL_WARP_RADIUS.get();
        double radiusSqr = radius * radius;
        double stopRadius = Math.min(radius, CrownServerConfig.TEMPORAL_WARP_STOP_RADIUS.get());
        double stopRadiusSqr = stopRadius * stopRadius;
        AABB box = player.getBoundingBox().inflate(radius);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.distanceToSqr(player) <= radiusSqr)) {
            if (!isHostileTo(player, living)) continue;
            CompoundTag livingData = living.getPersistentData();
            if (living.distanceToSqr(player) <= stopRadiusSqr
                    && !(living instanceof Player) && !CrownLogic.isBoss(living)) {
                livingData.putLong(NBT_TIME_STOPPED_UNTIL, now + TIME_WARP_SCAN_INTERVAL + 1L);
                if (!livingData.contains(NBT_TIME_STOPPED_X)) {
                    livingData.putDouble(NBT_TIME_STOPPED_X, living.getX());
                    livingData.putDouble(NBT_TIME_STOPPED_Y, living.getY());
                    livingData.putDouble(NBT_TIME_STOPPED_Z, living.getZ());
                }
            } else {
                livingData.putLong(NBT_TIME_SLOWED_UNTIL, now + TIME_WARP_SCAN_INTERVAL + 1L);
            }
        }
        Set<UUID> frozen = TIME_WARP_PROJECTILES.computeIfAbsent(player.getUUID(), k -> ConcurrentHashMap.newKeySet());
        for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, box, e -> e.distanceToSqr(player) <= radiusSqr)) {
            frozen.add(projectile.getUUID());
            projectile.setNoGravity(true);
            projectile.setDeltaMovement(Vec3.ZERO);
            projectile.hurtMarked = true;
        }
        if (now % 5L == 0L) renderTimeWarpBoundary(level, player);
    }

    private static boolean isHostileTo(ServerPlayer player, LivingEntity entity) {
        if (entity instanceof ServerPlayer other) {
            try {
                if (player.isAlliedTo(other) || other.isAlliedTo(player)) return false;
            } catch (RuntimeException ignored) {}
            return !FTBTeamsCompat.sameTeam(player, other);
        }
        if (entity instanceof Enemy) return true;
        return entity instanceof Mob mob && mob.getTarget() == player;
    }

    private static void renderTimeWarpBoundary(ServerLevel level, ServerPlayer player) {
        double cx = player.getX(), cy = player.getY() + 0.15D, cz = player.getZ();
        double radius = CrownServerConfig.TEMPORAL_WARP_RADIUS.get();
        int points = 48;
        for (int i = 0; i < points; i++) {
            double a = Math.PI * 2.0D * i / points;
            double x = cx + Math.cos(a) * radius;
            double z = cz + Math.sin(a) * radius;
            level.sendParticles(ParticleTypes.ENCHANT, x, cy, z, 1, 0, 0, 0, 0);
            if ((i & 3) == 0) level.sendParticles(ParticleTypes.END_ROD, x, cy + 2.0D, z, 1, 0, 0, 0, 0);
        }
        for (int i = 0; i < 24; i++) {
            double a = Math.PI * 2.0D * i / 24.0D;
            double x = cx + Math.cos(a) * radius;
            double z = cz + Math.sin(a) * radius;
            level.sendParticles(ParticleTypes.ENCHANT, x, cy + 4.0D, z, 1, 0, 0, 0, 0);
        }
    }

    private static void removeTimeWarpProjectiles(ServerPlayer player) {
        Set<UUID> ids = TIME_WARP_PROJECTILES.remove(player.getUUID());
        if (ids == null || ids.isEmpty()) return;
        MinecraftServer server = player.server;
        for (UUID id : ids) {
            for (ServerLevel level : server.getAllLevels()) {
                Entity entity = level.getEntity(id);
                if (entity instanceof Projectile projectile) {
                    projectile.discard();
                    break;
                }
            }
        }
    }

    public static void activateSanctify(ServerPlayer player) {
        if (!isWearingDivine(player)) return;
        long now = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        if (now < data.getLong(NBT_DIVINE_READY)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        double radius = CrownServerConfig.DIVINE_SANCTIFY_RADIUS.get();
        double radiusSqr = radius * radius;
        List<LivingEntity> allies = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                e -> e.distanceToSqr(player) <= radiusSqr && isSanctifyAlly(player, e)));
        if (!allies.contains(player)) allies.add(player);
        int cooldown = divineSanctifyCooldownTicks();
        if (allies.size() == 1) {
            cooldown = Math.max(0, cooldown - CrownServerConfig.DIVINE_SANCTIFY_SOLO_REDUCTION_SECONDS.get() * 20);
        }
        data.putLong(NBT_DIVINE_READY, now + cooldown);
        for (LivingEntity living : allies) {
            for (MobEffectInstance effect : new ArrayList<>(living.getActiveEffects())) {
                if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) living.removeEffect(effect.getEffect());
            }
            living.setHealth(living.getMaxHealth());
            level.sendParticles(ParticleTypes.END_ROD, living.getX(), living.getY() + living.getBbHeight() * 0.5D, living.getZ(),
                    12, 0.35D, 0.55D, 0.35D, 0.02D);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.2F);
        syncTooltipState(player);
    }

    public static boolean isSanctifyAlly(ServerPlayer player, LivingEntity other) {
        if (other == player) return true;
        try { if (player.isAlliedTo(other) || other.isAlliedTo(player)) return true; } catch (RuntimeException ignored) {}
        if (other instanceof ServerPlayer serverPlayer && FTBTeamsCompat.sameTeam(player, serverPlayer)) return true;
        if (other instanceof OwnableEntity owned) {
            try { return player.getUUID().equals(owned.getOwnerUUID()); } catch (RuntimeException ignored) {}
        }
        return false;
    }

    public static void activateLiberation(ServerPlayer player) {
        if (!hasCursedPower(player)) return;
        long now = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        if (now < data.getLong(NBT_CURSED_READY)) return;
        float oldMax = player.getMaxHealth();
        float oldHealth = player.getHealth();
        data.putLong(NBT_CURSED_LIBERATION_UNTIL, now + cursedLiberationDurationTicks());
        data.putLong(NBT_CURSED_READY, now + cursedLiberationCooldownTicks());
        syncPlayerAttributes(player);
        GlitchedFusionLogic.tickPlayer(player);
        player.setHealth(Math.min(player.getMaxHealth(), oldHealth + oldMax
                * CrownServerConfig.CURSED_LIBERATION_HEAL_MAX_HEALTH_MULTIPLIER.get().floatValue()));

        if (player.level() instanceof ServerLevel level) {
            double radius = CrownServerConfig.CURSED_LIBERATION_RADIUS.get();
            double radiusSqr = radius * radius;
            AABB box = player.getBoundingBox().inflate(radius);
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != player && e.distanceToSqr(player) <= radiusSqr)) {
                living.getPersistentData().putLong(NBT_CURSE_TRANSFER_UNTIL, now + cursedLiberationDurationTicks());
                syncTransferredCurse(living, true);
                level.sendParticles(ParticleTypes.SOUL, living.getX(), living.getY() + living.getBbHeight() * 0.5D, living.getZ(),
                        6, 0.3D, 0.4D, 0.3D, 0.01D);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.65F, 1.6F);
        }
        syncTooltipState(player);
    }

    public static int cursedLootingBonus(LivingEntity attacker) {
        return hasCursedPower(attacker) ? CrownServerConfig.CURSED_LOOTING_BONUS.get() : 0;
    }

    public static double cursedExperienceBonusFraction(Player player) {
        return player != null && hasCursedPower(player)
                ? CrownServerConfig.CURSED_EXPERIENCE_BONUS_FRACTION.get() : 0.0D;
    }

    public static void wipeCursedDeathExperience(ServerPlayer player) {
        if (!hasCursedPower(player) && !hasCursedCrownPendingDeath(player)) return;
        player.totalExperience = 0;
        player.experienceLevel = 0;
        player.experienceProgress = 0.0F;
        player.getPersistentData().putBoolean("thecrowns_cursed_died_zero_xp", true);
    }

    public static void resetCursedLiberationCooldown(ServerPlayer player) {
        if (player == null) return;
        player.getPersistentData().remove(NBT_CURSED_READY);
        syncTooltipState(player);
    }

    public static void resetCooldowns(ServerPlayer player, String crown) {
        if (player == null || crown == null) return;
        CompoundTag data = player.getPersistentData();
        switch (crown) {
            case "temporal" -> {
                data.remove(NBT_TIME_REWIND_READY);
                data.remove(NBT_TIME_WARP_READY);
            }
            case "frost" -> {
                data.remove(NBT_FROST_NEXT_STACK);
                data.remove(NBT_FROST_REFREEZE_READY);
            }
            case "divine" -> {
                data.remove(NBT_DIVINE_READY);
                data.remove(NBT_DIVINE_PASSIVE_READY);
            }
            case "cursed" -> data.remove(NBT_CURSED_READY);
            default -> {
                return;
            }
        }
        syncTooltipState(player);
    }

    /**
     * Moves every Temporal/Cursed Crown into a single-use death ticket before Forge posts
     * LivingDeathEvent. This is deliberately a death-only path: no player tick can create a
     * ticket from an absent item, so the 1.5.1 normal-play duplication bug cannot recur.
     */
    public static void prepareCrownDeathRetention(ServerPlayer player) {
        if (player == null) return;
        removeTimeWarpProjectiles(player);
        player.getPersistentData().remove(NBT_TIME_WARP_UNTIL);
        CompoundTag data = player.getPersistentData();

        // A canceled/deferred death may invoke ForgeHooks more than once before the next tick.
        // The existing ticket already owns the removed stacks and must remain authoritative.
        if (data.contains(NBT_TIME_KEEP_ITEMS, Tag.TAG_LIST)) return;

        ListTag kept = new ListTag();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!isDeathKeptCrown(stack)) continue;
            kept.add(makeDeathKeepEntry(stack, "inventory", null, i));
            player.getInventory().setItem(i, ItemStack.EMPTY);
        }
        try {
            CuriosApi.getCuriosInventory(player).resolve().ifPresent(inv -> {
                for (var curioEntry : inv.getCurios().entrySet()) {
                    var curio = curioEntry.getValue();
                    var stacks = curio.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        if (!isDeathKeptCrown(stack)) continue;
                        kept.add(makeDeathKeepEntry(stack, "curios", curioEntry.getKey(), i));
                        stacks.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            });
        } catch (RuntimeException ignored) {}
        if (!kept.isEmpty()) data.put(NBT_TIME_KEEP_ITEMS, kept);
    }

    private static boolean isDeathKeptCrown(ItemStack stack) {
        return stack != null && !stack.isEmpty()
                && (stack.is(ModItems.TEMPORAL_CROWN.get()) || stack.is(ModItems.CURSED_CROWN.get()));
    }

    private static CompoundTag makeDeathKeepEntry(ItemStack stack, String location, String curioId, int slot) {
        UUID keepId = UUID.randomUUID();
        stack.getOrCreateTag().putUUID(TAG_DEATH_KEEP_ID, keepId);
        CompoundTag entry = new CompoundTag();
        entry.putString("location", location);
        if (curioId != null) entry.putString("curio", curioId);
        entry.putInt("slot", slot);
        entry.putUUID("keepId", keepId);
        entry.put("stack", stack.save(new CompoundTag()));
        return entry;
    }

    public static boolean hasCursedCrownPendingDeath(ServerPlayer player) {
        if (player == null) return false;
        if (isWearingCursed(player)) return true;
        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(NBT_CURSED_RESPAWN_PENDING)) return true;
        if (!data.contains(NBT_TIME_KEEP_ITEMS, Tag.TAG_LIST)) return false;
        ListTag kept = data.getList(NBT_TIME_KEEP_ITEMS, Tag.TAG_COMPOUND);
        for (int i = 0; i < kept.size(); i++) {
            ItemStack stack = ItemStack.of(kept.getCompound(i).getCompound("stack"));
            if (stack.is(ModItems.CURSED_CROWN.get())) return true;
        }
        return false;
    }

    /** Consume and restore the death ticket once; marker lookup suppresses external duplicate keeps. */
    private static void restoreDeathKeptCrowns(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(NBT_TIME_KEEP_ITEMS, Tag.TAG_LIST)) return;

        // Consume before touching an inventory. If any callback throws, the same serialized
        // Crown cannot be materialized again by a later tick.
        ListTag kept = data.getList(NBT_TIME_KEEP_ITEMS, Tag.TAG_COMPOUND).copy();
        data.remove(NBT_TIME_KEEP_ITEMS);

        for (int i = 0; i < kept.size(); i++) {
            CompoundTag entry = kept.getCompound(i);
            ItemStack stack = ItemStack.of(entry.getCompound("stack"));
            if (stack.isEmpty()) continue;

            if (entry.hasUUID("keepId") && removeExistingDeathKeepMarker(player, entry.getUUID("keepId"))) {
                continue;
            }
            if (stack.getTag() != null) stack.getTag().remove(TAG_DEATH_KEEP_ID);

            int slot = entry.getInt("slot");
            boolean restored = false;
            if ("curios".equals(entry.getString("location"))) {
                restored = restoreCuriosDeathKeep(player, entry.getString("curio"), slot, stack);
            } else if (slot >= 0 && slot < player.getInventory().getContainerSize()
                    && player.getInventory().getItem(slot).isEmpty()) {
                player.getInventory().setItem(slot, stack);
                restored = true;
            }
            if (!restored && !player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }

    private static boolean restoreCuriosDeathKeep(ServerPlayer player, String curioId, int slot, ItemStack stack) {
        try {
            return CuriosApi.getCuriosInventory(player).resolve().map(inv -> {
                var curio = inv.getCurios().get(curioId);
                if (curio == null) return false;
                var stacks = curio.getStacks();
                if (slot < 0 || slot >= stacks.getSlots() || !stacks.getStackInSlot(slot).isEmpty()) return false;
                stacks.setStackInSlot(slot, stack);
                return true;
            }).orElse(false);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean removeExistingDeathKeepMarker(ServerPlayer player, UUID keepId) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (removeDeathKeepMarker(stack, keepId)) return true;
        }
        try {
            return CuriosApi.getCuriosInventory(player).resolve().map(inv -> {
                for (var curio : inv.getCurios().values()) {
                    var stacks = curio.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        if (removeDeathKeepMarker(stacks.getStackInSlot(i), keepId)) return true;
                    }
                }
                return false;
            }).orElse(false);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean removeDeathKeepMarker(ItemStack stack, UUID keepId) {
        CompoundTag tag = stack == null ? null : stack.getTag();
        if (tag == null || !tag.hasUUID(TAG_DEATH_KEEP_ID) || !keepId.equals(tag.getUUID(TAG_DEATH_KEEP_ID))) {
            return false;
        }
        tag.remove(TAG_DEATH_KEEP_ID);
        return true;
    }


    /**
     * Keeps only the equipped-state marker for the Cursed Crown and performs the one-shot
     * post-death restore for a vanilla head-slot Crown. Ordinary inventory interaction is
     * blocked before the server mutates the slot, so this method must never manufacture a
     * replacement copy during normal play. This is intentionally different from the old
     * 1.5.1 snapshot/forced-restore path, which could duplicate the Crown when a menu or
     * another mod briefly moved the original stack before the next player tick.
     */
    private static void maintainCursedBinding(ServerPlayer player) {
        restoreCursedCrownAfterDeath(player);

        ItemStack equipped = findEquipped(player, ModItems.CURSED_CROWN.get());
        if (!equipped.isEmpty()) {
            equipped.getOrCreateTag().putBoolean(TAG_CURSED_BOUND, true);
            return;
        }

        // A legal removal ends the active binding marker. There is deliberately no generic
        // "if missing, recreate from NBT" fallback here: removal is prevented at the slot /
        // packet boundary instead, which guarantees a single authoritative ItemStack.
        if (canRemoveCursedCrown(player)) {
            releaseCursedBinding(player);
        }
    }

    /**
     * Reject any normal container click that tries to interact with an equipped Cursed Crown
     * while the wearer does not satisfy a removal exception. This catches vanilla armor,
     * Curios menus, number-key swaps, shift-clicks, throws, and drag updates at the packet
     * boundary before the underlying slot can be changed.
     */
    public static boolean shouldBlockCursedCrownContainerClick(ServerPlayer player, ServerboundContainerClickPacket packet) {
        if (player == null || packet == null || canRemoveCursedCrown(player)) return false;
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null || packet.getContainerId() != menu.containerId) return false;

        if (isCursedSlot(menu, packet.getSlotNum())) return true;
        for (int slotId : packet.getChangedSlots().keySet()) {
            if (isCursedSlot(menu, slotId)) return true;
        }
        return false;
    }

    private static boolean isCursedSlot(AbstractContainerMenu menu, int slotId) {
        if (slotId < 0 || slotId >= menu.slots.size()) return false;
        ItemStack stack = menu.getSlot(slotId).getItem();
        return !stack.isEmpty() && stack.is(ModItems.CURSED_CROWN.get());
    }

    /** Restore exactly the stack serialized by prepareCursedCrownDeath(), once. */
    private static void restoreCursedCrownAfterDeath(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(NBT_CURSED_RESPAWN_PENDING)) return;

        // Consume the restore ticket before mutating inventory so an exception cannot cause
        // the same serialized stack to be materialized twice on subsequent ticks.
        CompoundTag saved = data.contains(NBT_CURSED_BOUND_STACK, Tag.TAG_COMPOUND)
                ? data.getCompound(NBT_CURSED_BOUND_STACK).copy() : new CompoundTag();
        data.remove(NBT_CURSED_RESPAWN_PENDING);
        data.remove(NBT_CURSED_BOUND_STACK);

        ItemStack restored = ItemStack.of(saved);
        if (restored.isEmpty()) return;

        // If another system already kept the same type equipped, the death copy is discarded.
        // Curios uses ALWAYS_KEEP and normally reaches this branch only for the vanilla head slot.
        if (isWearingCursed(player)) return;

        restored.getOrCreateTag().putBoolean(TAG_CURSED_BOUND, true);
        ItemStack displaced = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!displaced.isEmpty()) {
            ItemStack displacedCopy = displaced.copy();
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            if (!player.getInventory().add(displacedCopy)) player.drop(displacedCopy, false);
        }
        player.setItemSlot(EquipmentSlot.HEAD, restored);
    }


    public static void releaseCursedBinding(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        // A death restore ticket owns its serialized stack until restoreCursedCrownAfterDeath()
        // consumes it. Ordinary legal unequip must not erase that ticket mid-respawn.
        if (!data.getBoolean(NBT_CURSED_RESPAWN_PENDING)) data.remove(NBT_CURSED_BOUND_STACK);
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.CURSED_CROWN.get()) && stack.getTag() != null) {
                stack.getTag().remove(TAG_CURSED_BOUND);
            }
        }
        try {
            CuriosApi.getCuriosInventory(player).resolve().ifPresent(inv -> {
                for (var curio : inv.getCurios().values()) {
                    var stacks = curio.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        if (stack.is(ModItems.CURSED_CROWN.get()) && stack.getTag() != null) {
                            stack.getTag().remove(TAG_CURSED_BOUND);
                        }
                    }
                }
            });
        } catch (RuntimeException ignored) {}
    }

    public static void releaseCursedBindingIfAllowed(ServerPlayer player) {
        if (player == null || !canRemoveCursedCrown(player) || isWearingCursed(player)) return;
        releaseCursedBinding(player);
    }

    /** Remove a head-slot Cursed Crown from ordinary death drops; its serialized bound copy survives clone. */
    public static void prepareCursedCrownDeath(ServerPlayer player) {
        if (player == null) return;
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.is(ModItems.CURSED_CROWN.get())) return;
        head.getOrCreateTag().putBoolean(TAG_CURSED_BOUND, true);
        CompoundTag data = player.getPersistentData();
        data.put(NBT_CURSED_BOUND_STACK, head.save(new CompoundTag()));
        data.putBoolean(NBT_CURSED_RESPAWN_PENDING, true);
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
    }

    private static void transformCursedCrowns(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            ItemStack transformed = transformIfCursed(stack);
            if (transformed != stack) player.getInventory().setItem(i, transformed);
        }
        try {
            CuriosApi.getCuriosInventory(player).resolve().ifPresent(inv -> {
                for (var curio : inv.getCurios().values()) {
                    var stacks = curio.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        ItemStack transformed = transformIfCursed(stack);
                        if (transformed != stack) stacks.setStackInSlot(i, transformed);
                    }
                }
            });
        } catch (RuntimeException ignored) {}
    }

    private static ItemStack transformIfCursed(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.is(ModItems.CURSED_CROWN.get())) return stack;
        if (!isCurseTransformSource(stack)) return stack;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        Map<Enchantment, Integer> retainedEnchantments = new LinkedHashMap<>();
        boolean curse = false;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (enchantment != null && enchantment.isCurse()) {
                curse = true;
            } else if (enchantment != null) {
                retainedEnchantments.put(enchantment, entry.getValue());
            }
        }
        if (!curse) return stack;
        ItemStack out = new ItemStack(ModItems.CURSED_CROWN.get(), stack.getCount());
        if (stack.getTag() != null) out.setTag(stack.getTag().copy());
        // Copy all non-enchantment NBT first, then rewrite the enchantment list so every
        // Enchantment#isCurse() entry—including the trigger curse—is removed.
        EnchantmentHelper.setEnchantments(retainedEnchantments, out);
        out.setDamageValue(Math.min(out.getMaxDamage() - 1, stack.getDamageValue()));
        return out;
    }

    /** Explicit Tier I-III allowlist; future Standard Crown subclasses do not become cursed accidentally. */
    public static boolean isCurseTransformSource(ItemStack stack) {
        return stack != null && !stack.isEmpty() && (
                stack.is(ModItems.BURNING_CROWN.get())
                        || stack.is(ModItems.IRONFORGED_CROWN.get())
                        || stack.is(ModItems.FROST_CROWN.get())
                        || stack.is(ModItems.BLOODY_CROWN.get())
                        || stack.is(ModItems.DARKENED_CROWN.get())
                        || stack.is(ModItems.WARRIOR_CROWN.get())
                        || stack.is(ModItems.DIVINE_CROWN.get())
                        || stack.is(ModItems.CROWN_OF_LIGHT.get())
                        || stack.is(ModItems.DIMENSIONAL_CROWN.get())
                        || stack.is(ModItems.ANGELIC_CROWN.get())
                        || stack.is(ModItems.TEMPORAL_CROWN.get()));
    }

    private static ItemStack findEquipped(ServerPlayer player, net.minecraft.world.item.Item item) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (head.is(item)) return head;
        try {
            return CuriosApi.getCuriosInventory(player).resolve().map(inv -> {
                for (var curio : inv.getCurios().values()) {
                    var stacks = curio.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        if (stack.is(item)) return stack;
                    }
                }
                return ItemStack.EMPTY;
            }).orElse(ItemStack.EMPTY);
        } catch (RuntimeException ignored) { return ItemStack.EMPTY; }
    }

    private static void syncTooltipState(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) syncTooltipStack(player.getInventory().getItem(i), data);
        try {
            CuriosApi.getCuriosInventory(player).resolve().ifPresent(inv -> {
                for (var curio : inv.getCurios().values()) {
                    var stacks = curio.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) syncTooltipStack(stacks.getStackInSlot(i), data);
                }
            });
        } catch (RuntimeException ignored) {}
    }

    private static void syncTooltipStack(ItemStack stack, CompoundTag data) {
        if (stack == null || stack.isEmpty()) return;
        if (stack.is(ModItems.TEMPORAL_CROWN.get())) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putLong(TAG_TIME_REWIND_READY_AT, data.getLong(NBT_TIME_REWIND_READY));
            tag.putLong(TAG_TIME_WARP_READY_AT, data.getLong(NBT_TIME_WARP_READY));
            tag.putBoolean(TAG_TIME_SNAPSHOT, data.contains(NBT_TIME_SNAPSHOT, Tag.TAG_COMPOUND));
        } else if (stack.is(ModItems.DIVINE_CROWN.get())) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putLong(TAG_DIVINE_READY_AT, data.getLong(NBT_DIVINE_READY));
        } else if (stack.is(ModItems.CURSED_CROWN.get())) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putLong(TAG_CURSED_READY_AT, data.getLong(NBT_CURSED_READY));
        } else if (stack.getTag() != null && stack.getTag().isEmpty()) {
            stack.setTag(null);
        }
    }

    public static long tooltipLong(ItemStack stack, String key) {
        return stack != null && !stack.isEmpty() && stack.getTag() != null ? stack.getTag().getLong(key) : 0L;
    }

    public static boolean tooltipBoolean(ItemStack stack, String key) {
        return stack != null && !stack.isEmpty() && stack.getTag() != null && stack.getTag().getBoolean(key);
    }

    /** Config-aware text shared by item tooltips and the lorebook. */
    public static Component configuredDescription(String key) {
        return switch (key) {
            case "tooltip.thecrowns.temporal_crown.stat.1" -> Component.translatable(key, percent(CrownServerConfig.TEMPORAL_MOVEMENT_SPEED_BONUS.get()));
            case "tooltip.thecrowns.temporal_crown.stat.2" -> Component.translatable(key, percent(CrownServerConfig.TEMPORAL_ATTACK_SPEED_BONUS.get()));
            case "tooltip.thecrowns.temporal_crown.passive.2" -> Component.translatable(key, CrownServerConfig.TEMPORAL_OUT_OF_COMBAT_SECONDS.get());
            case "tooltip.thecrowns.temporal_crown.rewind.2" -> Component.translatable(key,
                    CrownServerConfig.TEMPORAL_REWIND_CONCENTRATION_SECONDS.get());
            case "tooltip.thecrowns.temporal_crown.warp.1" -> Component.translatable(key,
                    number(CrownServerConfig.TEMPORAL_WARP_STOP_RADIUS.get()), number(CrownServerConfig.TEMPORAL_WARP_RADIUS.get()),
                    percent(CrownServerConfig.TEMPORAL_WARP_SLOW_FRACTION.get()));
            case "gui.thecrowns.lorebook.temporal.rewind.meta" -> Component.translatable(key, CrownServerConfig.TEMPORAL_REWIND_COOLDOWN_SECONDS.get());
            case "gui.thecrowns.lorebook.temporal.warp.meta" -> Component.translatable(key,
                    CrownServerConfig.TEMPORAL_WARP_DURATION_SECONDS.get(), CrownServerConfig.TEMPORAL_WARP_COOLDOWN_SECONDS.get());

            case "tooltip.thecrowns.frost_crown.stat.1" -> Component.translatable(key, number(CrownServerConfig.FROST_ATTACK_DAMAGE.get()));
            case "tooltip.thecrowns.frost_crown.stat.2" -> Component.translatable(key, number(CrownServerConfig.FROST_ARMOR.get()));
            case "tooltip.thecrowns.frost_crown.stat.3" -> Component.translatable(key, number(CrownServerConfig.FROST_TOUGHNESS.get()));
            case "tooltip.thecrowns.frost_crown.passive.3" -> Component.translatable(key,
                    number(CrownServerConfig.FROST_STACK_INTERNAL_COOLDOWN_SECONDS.get()), percent(CrownServerConfig.FROST_SLOW_PER_STACK.get()),
                    CrownServerConfig.FROST_STACKS_TO_FREEZE.get(), CrownServerConfig.FROST_FREEZE_SECONDS.get(),
                    CrownServerConfig.FROST_BOSS_PLAYER_FREEZE_SECONDS.get(),
                    CrownServerConfig.FROST_REFREEZE_COOLDOWN_SECONDS.get(), CrownServerConfig.FROST_BOSS_PLAYER_REFREEZE_COOLDOWN_SECONDS.get(),
                    CrownServerConfig.FROST_DECAY_SECONDS.get());
            case "tooltip.thecrowns.frost_crown.passive.4" -> Component.translatable(key, number(CrownServerConfig.FROST_BONUS_DAMAGE.get()));

            case "tooltip.thecrowns.divine_crown.stat.1" -> Component.translatable(key, number(CrownServerConfig.DIVINE_FLAT_MAX_HEALTH.get()));
            case "tooltip.thecrowns.divine_crown.stat.2" -> Component.translatable(key, number(CrownServerConfig.DIVINE_ARMOR.get()));
            case "tooltip.thecrowns.divine_crown.stat.3" -> Component.translatable(key,
                    percent(CrownServerConfig.DIVINE_HEALING_MULTIPLIER.get() - 1.0D));
            case "tooltip.thecrowns.divine_crown.stat.4" -> Component.translatable(key, percent(CrownServerConfig.DIVINE_PERCENT_MAX_HEALTH.get()));
            case "tooltip.thecrowns.divine_crown.passive.1" -> Component.translatable(key,
                    number(CrownServerConfig.DIVINE_NEGATIVE_EFFECT_BASE_HEAL.get()),
                    percent(CrownServerConfig.DIVINE_NEGATIVE_EFFECT_MAX_HEALTH_FRACTION.get()));
            case "tooltip.thecrowns.divine_crown.passive.2" -> Component.translatable(key,
                    number(CrownServerConfig.DIVINE_NEGATIVE_EFFECT_COOLDOWN_SECONDS.get()));
            case "tooltip.thecrowns.divine_crown.active.1" -> Component.translatable(key, number(CrownServerConfig.DIVINE_SANCTIFY_RADIUS.get()));
            case "tooltip.thecrowns.divine_crown.active.2" -> Component.translatable(key,
                    CrownServerConfig.DIVINE_SANCTIFY_SOLO_REDUCTION_SECONDS.get());

            case "tooltip.thecrowns.cursed_crown.stat.1", "tooltip.thecrowns.cursed_crown.stat.2",
                    "tooltip.thecrowns.cursed_crown.stat.3", "tooltip.thecrowns.cursed_crown.stat.4" ->
                    Component.translatable(key, percent(CrownServerConfig.CURSED_PENALTY_FRACTION.get()));
            case "tooltip.thecrowns.cursed_crown.stat.5" -> Component.translatable(key, CrownServerConfig.CURSED_LOOTING_BONUS.get());
            case "tooltip.thecrowns.cursed_crown.stat.6" -> Component.translatable(key, number(CrownServerConfig.CURSED_LUCK_BONUS.get()));
            case "tooltip.thecrowns.cursed_crown.passive.1" -> Component.translatable(key,
                    percent(CrownServerConfig.CURSED_EXPERIENCE_BONUS_FRACTION.get()));
            case "tooltip.thecrowns.cursed_crown.active.1" -> Component.translatable(key,
                    CrownServerConfig.CURSED_LIBERATION_DURATION_SECONDS.get(), number(CrownServerConfig.CURSED_LIBERATION_RADIUS.get()));
            default -> Component.translatable(key);
        };
    }

    private static String percent(double fraction) { return number(fraction * 100.0D); }

    private static String number(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0000001D) return Long.toString(Math.round(value));
        String formatted = String.format(Locale.ROOT, "%.3f", value);
        return formatted.replaceFirst("0+$", "").replaceFirst("\\.$", "");
    }
}
