package com.thecrowns.logic;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.compat.FTBTeamsCompat;
import com.thecrowns.network.AngelicGuardEffectPayload;
import com.thecrowns.network.ModNetworking;
import com.thecrowns.registry.ModItems;
import com.thecrowns.util.ItemStackCustomData;
import net.minecraft.core.Holder;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;
import top.theillusivec4.curios.api.CuriosApi;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Gameplay implementation for the non-Glitched Crown families introduced in The Crowns 1.3.0. */
public final class NewCrownLogic {
    /** Technical cap with ample headroom below Float.MAX_VALUE; prevents NaN/Infinity propagation through modded damage stacks. */
    public static final float CROWN_DAMAGE_SAFETY_CAP = 1.0E30F;
    public static final int BLOODY_MAX_STACKS = 40;
    public static final int IRONFORGED_COOLDOWN_TICKS = 120 * 20;
    public static final int DARKENED_DODGE_COOLDOWN_TICKS = 8 * 20;
    public static final int DIMENSIONAL_CHARGE_TICKS = 12 * 20;
    public static final int BURNING_REFLECT_COOLDOWN_TICKS = 30;
    public static final int BLOODY_COOLDOWN_TICKS = 40 * 20;
    public static final int BLOODY_MELEE_HEAL_COOLDOWN_TICKS = 20;
    public static final int WARRIOR_DUEL_DURATION_TICKS = 15 * 20;
    public static final int WARRIOR_DUEL_COOLDOWN_TICKS = 120 * 20;
    public static final int ANGELIC_GUARD_COOLDOWN_TICKS = 600 * 20;
    public static final int ANGELIC_INVULNERABILITY_TICKS = 4 * 20;
    public static final int ANGELIC_FLIGHT_DURATION_TICKS = 30 * 20;
    public static final int ANGELIC_FLIGHT_COOLDOWN_TICKS = 120 * 20;

    private static final String NBT_BLOODY_STACKS = "thecrowns_bloody_kill_stacks";
    private static final String NBT_BLOODY_LAST_DAMAGE = "thecrowns_bloody_last_damage";
    private static final String NBT_BLOODY_LAST_WORN = "thecrowns_bloody_last_worn";
    private static final String NBT_BLOODY_READY = "thecrowns_bloody_ready";
    private static final String NBT_BLOODY_MELEE_HEAL_READY = "thecrowns_bloody_melee_heal_ready";
    private static final String NBT_DARKENED_DODGE_READY = "thecrowns_darkened_dodge_ready";
    private static final String NBT_DIMENSIONAL_CHARGE_READY = "thecrowns_dimensional_charge_ready";
    private static final String NBT_DIMENSIONAL_LAST_WORN = "thecrowns_dimensional_last_worn";
    private static final String NBT_IRONFORGED_READY = "thecrowns_ironforged_ready";
    private static final String NBT_IRONFORGED_OWNER = "thecrowns_ironforged_owner";
    private static final String NBT_IRONFORGED_GOLEM_UUID = "thecrowns_ironforged_golem_uuid";
    private static final String NBT_DIMENSIONAL_RESCUE_FALL = "thecrowns_dimensional_rescue_fall";
    private static final String NBT_DIMENSIONAL_RESCUE_AT = "thecrowns_dimensional_rescue_at";
    private static final String NBT_LIGHT_GRACE_UNTIL = "thecrowns_light_grace_until";
    private static final String NBT_LIGHT_DISABLED = "thecrowns_light_disabled";
    private static final String NBT_WARRIOR_READY = "thecrowns_warrior_ready";
    private static final String NBT_WARRIOR_DUEL_UNTIL = "thecrowns_warrior_duel_until";
    private static final String NBT_WARRIOR_TARGET = "thecrowns_warrior_target";
    private static final String NBT_WARRIOR_BLOCKED_MOTION_UNTIL = "thecrowns_warrior_blocked_motion_until";
    private static final String NBT_WARRIOR_BLOCKED_MOTION_X = "thecrowns_warrior_blocked_motion_x";
    private static final String NBT_WARRIOR_BLOCKED_MOTION_Y = "thecrowns_warrior_blocked_motion_y";
    private static final String NBT_WARRIOR_BLOCKED_MOTION_Z = "thecrowns_warrior_blocked_motion_z";
    private static final String NBT_ANGELIC_GUARD_READY = "thecrowns_angelic_guard_ready";
    private static final String NBT_ANGELIC_INVULN_UNTIL = "thecrowns_angelic_invuln_until";
    private static final String NBT_ANGELIC_FLIGHT_READY = "thecrowns_angelic_flight_ready";
    private static final String NBT_ANGELIC_FLIGHT_UNTIL = "thecrowns_angelic_flight_until";
    private static final String NBT_ANGELIC_GRANTED_FLIGHT = "thecrowns_angelic_granted_flight";
    private static final String NBT_ANGELIC_HAD_MAYFLY = "thecrowns_angelic_had_mayfly";
    private static final String NBT_ANGELIC_SPEED_ACTIVE = "thecrowns_angelic_speed_active";
    private static final String NBT_ANGELIC_BASE_FLY_SPEED = "thecrowns_angelic_base_fly_speed";
    private static final String NBT_ANGELIC_GUARD_FULL_HEALTH_TICKS = "thecrowns_angelic_guard_full_health_ticks";

    public static final String TAG_BLOODY_STACKS = "TheCrownsBloodyStacks";
    public static final String TAG_BLOODY_READY_AT = "TheCrownsBloodyReadyAt";
    public static final String TAG_IRONFORGED_READY_AT = "TheCrownsIronforgedReadyAt";
    public static final String TAG_WARRIOR_READY_AT = "TheCrownsWarriorReadyAt";
    public static final String TAG_ANGELIC_GUARD_READY_AT = "TheCrownsAngelicGuardReadyAt";
    public static final String TAG_ANGELIC_FLIGHT_READY_AT = "TheCrownsAngelicFlightReadyAt";

    private static final ResourceLocation LIGHT_HEALTH = modifierId("light_health");
    private static final ResourceLocation LIGHT_ARMOR = modifierId("light_armor");
    private static final ResourceLocation LIGHT_TOUGHNESS = modifierId("light_toughness");
    private static final ResourceLocation LIGHT_SPEED = modifierId("light_speed");

    private static final ResourceLocation BLOODY_HEALTH_FLAT = modifierId("bloody_health_flat");
    private static final ResourceLocation BLOODY_HEALTH_PERCENT = modifierId("bloody_health_percent");
    private static final ResourceLocation BLOODY_KILL_HEALTH = modifierId("bloody_kill_health");

    private static final ResourceLocation BURNING_HEALTH = modifierId("burning_health");
    private static final ResourceLocation BURNING_ARMOR = modifierId("burning_armor");
    private static final ResourceLocation BURNING_TOUGHNESS = modifierId("burning_toughness");

    private static final ResourceLocation DARKENED_ARMOR = modifierId("darkened_armor");
    private static final ResourceLocation DARKENED_SPEED = modifierId("darkened_speed");

    private static final ResourceLocation IRONFORGED_ARMOR = modifierId("ironforged_armor");
    private static final ResourceLocation IRONFORGED_TOUGHNESS = modifierId("ironforged_toughness");
    private static final ResourceLocation IRONFORGED_SPEED = modifierId("ironforged_speed");

    private static final ResourceLocation DIMENSIONAL_SPEED = modifierId("dimensional_speed");
    private static final ResourceLocation DIMENSIONAL_DAMAGE = modifierId("dimensional_damage");
    private static final ResourceLocation DIMENSIONAL_ATTACK_SPEED = modifierId("dimensional_attack_speed");

    private static final ResourceLocation WARRIOR_HEALTH = modifierId("warrior_health");
    private static final ResourceLocation WARRIOR_DAMAGE = modifierId("warrior_damage");
    private static final ResourceLocation WARRIOR_ARMOR = modifierId("warrior_armor");
    private static final ResourceLocation WARRIOR_TOUGHNESS = modifierId("warrior_toughness");
    private static final ResourceLocation WARRIOR_DUEL_ATTACK_SPEED = modifierId("warrior_duel_attack_speed");

    private static final ResourceLocation ANGELIC_HEALTH = modifierId("angelic_health");
    private static final ResourceLocation ANGELIC_TOUGHNESS = modifierId("angelic_toughness");
    private static final ResourceLocation ANGELIC_SPEED = modifierId("angelic_speed");

    private static final ConcurrentHashMap<ReflectionKey, Long> REFLECTION_COOLDOWNS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<WardenPlayerKey, Long> WARDEN_AGGRO = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> REFLECTION_GUARD = ThreadLocal.withInitial(() -> false);

    private NewCrownLogic() {}

    private static ResourceLocation modifierId(String path) {
        return ResourceLocation.fromNamespaceAndPath(TheCrownsMod.MOD_ID, path);
    }

    public static boolean isWearingCrownOfLight(LivingEntity entity) { return isWearing(entity, ModItems.CROWN_OF_LIGHT.get()); }
    public static boolean isWearingBloody(LivingEntity entity) { return isWearing(entity, ModItems.BLOODY_CROWN.get()); }
    public static boolean isWearingBurning(LivingEntity entity) { return isWearing(entity, ModItems.BURNING_CROWN.get()); }
    public static boolean isWearingDarkened(LivingEntity entity) { return isWearing(entity, ModItems.DARKENED_CROWN.get()); }
    public static boolean isWearingIronforged(LivingEntity entity) { return isWearing(entity, ModItems.IRONFORGED_CROWN.get()); }
    public static boolean isWearingDimensional(LivingEntity entity) { return isWearing(entity, ModItems.DIMENSIONAL_CROWN.get()); }
    public static boolean isWearingWarrior(LivingEntity entity) { return isWearing(entity, ModItems.WARRIOR_CROWN.get()); }
    public static boolean isWearingAngelic(LivingEntity entity) { return isWearing(entity, ModItems.ANGELIC_CROWN.get()); }
    public static boolean hasIronforgedGuardianPower(LivingEntity entity) {
        return isWearingIronforged(entity) || GlitchedFusionLogic.has(entity, ModItems.IRONFORGED_CROWN.get());
    }

    private static boolean hasLightPower(LivingEntity entity) {
        return isWearingCrownOfLight(entity) || GlitchedFusionLogic.has(entity, ModItems.CROWN_OF_LIGHT.get());
    }

    private static void tickLightGrace(ServerPlayer player, long now) {
        CompoundTag data = player.getPersistentData();
        if (!hasLightPower(player)) {
            data.remove(NBT_LIGHT_GRACE_UNTIL);
            data.remove(NBT_LIGHT_DISABLED);
            return;
        }
        isLightEmpowered(player);
    }

    /** Light powers remain active for one second after health first reaches 50% or less. */
    public static boolean isLightEmpowered(ServerPlayer player) {
        if (player == null || !hasLightPower(player) || player.getMaxHealth() <= 0.0F) return false;
        CompoundTag data = player.getPersistentData();
        long now = player.level().getGameTime();
        if (player.getHealth() > player.getMaxHealth() * 0.5F) {
            data.remove(NBT_LIGHT_GRACE_UNTIL);
            data.remove(NBT_LIGHT_DISABLED);
            return true;
        }
        if (data.getBoolean(NBT_LIGHT_DISABLED)) return false;
        if (!data.contains(NBT_LIGHT_GRACE_UNTIL)) {
            data.putLong(NBT_LIGHT_GRACE_UNTIL, now + 20L);
            return true;
        }
        if (now < data.getLong(NBT_LIGHT_GRACE_UNTIL)) return true;
        data.putBoolean(NBT_LIGHT_DISABLED, true);
        return false;
    }

    public static boolean isAngelicCreativeFlightActive(Player player) {
        return player != null && player.getPersistentData().getLong(NBT_ANGELIC_FLIGHT_UNTIL)
                > player.level().getGameTime();
    }

    private static boolean hasDimensionalCombatPower(LivingEntity entity) {
        return isWearingDimensional(entity) || GlitchedFusionLogic.has(entity, ModItems.DIMENSIONAL_CROWN.get());
    }

    public static boolean isWearing(LivingEntity entity, Item item) {
        if (entity == null || item == null) return false;
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!helmet.isEmpty() && helmet.is(item)) return true;
        try {
            return CuriosApi.getCuriosInventory(entity).map(inv -> {
                for (var entry : inv.getCurios().values()) {
                    var stacks = entry.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        if (!stack.isEmpty() && stack.is(item)) return true;
                    }
                }
                return false;
            }).orElse(false);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static void copyPermanentPlayerData(Player original, Player replacement) {
        if (original == null || replacement == null) return;
        CompoundTag src = original.getPersistentData();
        CompoundTag dst = replacement.getPersistentData();
        dst.putInt(NBT_BLOODY_STACKS, Math.max(0, Math.min(BLOODY_MAX_STACKS, src.getInt(NBT_BLOODY_STACKS))));
        copyLong(src, dst, NBT_BLOODY_READY, NBT_DARKENED_DODGE_READY, NBT_DIMENSIONAL_CHARGE_READY,
                NBT_IRONFORGED_READY, NBT_WARRIOR_READY, NBT_ANGELIC_GUARD_READY, NBT_ANGELIC_FLIGHT_READY);
    }

    private static void copyLong(CompoundTag src, CompoundTag dst, String... keys) {
        for (String key : keys) if (src.contains(key)) dst.putLong(key, src.getLong(key));
    }

    public static int getBloodyStacks(Player player) {
        return player == null ? 0 : Math.max(0, Math.min(BLOODY_MAX_STACKS,
                player.getPersistentData().getInt(NBT_BLOODY_STACKS)));
    }

    public static void resetCooldowns(ServerPlayer player, String crown) {
        if (player == null || crown == null) return;
        CompoundTag data = player.getPersistentData();
        switch (crown) {
            case "bloody" -> {
                data.remove(NBT_BLOODY_READY);
                data.remove(NBT_BLOODY_MELEE_HEAL_READY);
            }
            case "burning" -> REFLECTION_COOLDOWNS.keySet().removeIf(key -> key.wearer().equals(player.getUUID()));
            case "darkened" -> data.remove(NBT_DARKENED_DODGE_READY);
            case "dimensional" -> data.remove(NBT_DIMENSIONAL_CHARGE_READY);
            case "ironforged" -> data.remove(NBT_IRONFORGED_READY);
            case "warrior" -> data.remove(NBT_WARRIOR_READY);
            case "angelic" -> {
                data.remove(NBT_ANGELIC_GUARD_READY);
                data.remove(NBT_ANGELIC_FLIGHT_READY);
                data.remove(NBT_ANGELIC_GUARD_FULL_HEALTH_TICKS);
            }
            default -> {
                return;
            }
        }
        syncTooltipState(player);
    }

    public static long tooltipLong(ItemStack stack, String key, long fallback) {
        return ItemStackCustomData.getLong(stack, key, fallback);
    }

    public static int tooltipInt(ItemStack stack, String key, int fallback) {
        return ItemStackCustomData.getInt(stack, key, fallback);
    }

    public static void tickPlayer(ServerPlayer player) {
        long now = player.level().getGameTime();
        tickLightGrace(player, now);
        syncAttributes(player);

        if (isWearingBurning(player)) {
            player.clearFire();
            removeEffects(player, MobEffects.POISON, MobEffects.WITHER, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.CONFUSION);
        }
        if (isWearingDarkened(player)) {
            removeEffects(player, MobEffects.DARKNESS, MobEffects.BLINDNESS);
            tickDarkenedWardenAffinity(player, now);
        }
        if (isWearingBloody(player)) {
            CompoundTag data = player.getPersistentData();
            long lastWorn = data.getLong(NBT_BLOODY_LAST_WORN);
            if (lastWorn != now - 1L) data.putLong(NBT_BLOODY_LAST_DAMAGE, now);
            data.putLong(NBT_BLOODY_LAST_WORN, now);
            long lastDamage = data.getLong(NBT_BLOODY_LAST_DAMAGE);
            if (now - lastDamage >= 60L && now % 20L == 0L && player.getHealth() < player.getMaxHealth()) {
                player.heal(2.0F);
            }
        }
        boolean dimensionalCombat = hasDimensionalCombatPower(player);
        if (dimensionalCombat) {
            CompoundTag data = player.getPersistentData();
            long lastWorn = data.getLong(NBT_DIMENSIONAL_LAST_WORN);
            if (lastWorn != now - 1L) data.putLong(NBT_DIMENSIONAL_CHARGE_READY, now + DIMENSIONAL_CHARGE_TICKS);
            data.putLong(NBT_DIMENSIONAL_LAST_WORN, now);
        }

        if (isWearingDimensional(player)) {
            CompoundTag data = player.getPersistentData();

            if (player.getY() < -500.0D) {
                int max = player.level().getMaxBuildHeight();
                double rescueY = max <= 300 ? Math.max(player.level().getMinBuildHeight() + 2.0D, max - 2.0D) : 300.0D;
                player.teleportTo(player.getX(), rescueY, player.getZ());
                Vec3 velocity = player.getDeltaMovement();
                player.setDeltaMovement(velocity.x, Math.max(0.0D, velocity.y), velocity.z);
                player.fallDistance = 0.0F;
                data.putBoolean(NBT_DIMENSIONAL_RESCUE_FALL, true);
                data.putLong(NBT_DIMENSIONAL_RESCUE_AT, now);
            }
            if (data.getBoolean(NBT_DIMENSIONAL_RESCUE_FALL)) {
                player.fallDistance = 0.0F;
                long rescueAt = data.getLong(NBT_DIMENSIONAL_RESCUE_AT);
                if (now - rescueAt >= 30L * 20L) {
                    data.remove(NBT_DIMENSIONAL_RESCUE_FALL);
                    data.remove(NBT_DIMENSIONAL_RESCUE_AT);
                }
            }
        }

        tickWarriorBlockedMotion(player, now);
        tickAngelicFlight(player, now);
        tickAngelicGuardCooldown(player, now);
        tickOwnedIronGolem(player);
        syncTooltipState(player);

        if (player.tickCount % 200 == 0) {
            long cutoff = now - 20L * 60L;
            REFLECTION_COOLDOWNS.entrySet().removeIf(e -> e.getValue() < cutoff);
        }
    }

    private static void syncAttributes(ServerPlayer player) {
        boolean light = hasLightPower(player);
        boolean bloody = isWearingBloody(player);
        boolean burning = isWearingBurning(player);
        boolean darkened = isWearingDarkened(player);
        boolean ironforged = isWearingIronforged(player);
        boolean dimensional = isWearingDimensional(player);
        boolean warrior = isWearingWarrior(player);
        boolean angelic = isWearingAngelic(player);

        setModifier(player, Attributes.MAX_HEALTH, LIGHT_HEALTH, "crown_of_light_health", light ? 20.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        setModifier(player, Attributes.ARMOR, LIGHT_ARMOR, "crown_of_light_armor", light ? 15.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        setModifier(player, Attributes.ARMOR_TOUGHNESS, LIGHT_TOUGHNESS, "crown_of_light_toughness", light ? 7.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        boolean lightEmpowered = isLightEmpowered(player);
        setModifier(player, Attributes.MOVEMENT_SPEED, LIGHT_SPEED, "crown_of_light_speed", lightEmpowered ? 0.20D : 0.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        setModifier(player, Attributes.MAX_HEALTH, BLOODY_HEALTH_FLAT, "bloody_crown_health_flat", bloody ? 40.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        setModifier(player, Attributes.MAX_HEALTH, BLOODY_HEALTH_PERCENT, "bloody_crown_health_percent", bloody ? 0.15D : 0.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        // Bloody's earned health is permanent and remains after the Crown is removed.
        setModifier(player, Attributes.MAX_HEALTH, BLOODY_KILL_HEALTH, "bloody_crown_permanent_health", getBloodyStacks(player), AttributeModifier.Operation.ADD_VALUE);

        setModifier(player, Attributes.MAX_HEALTH, BURNING_HEALTH, "burning_crown_health", burning ? 10.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        setModifier(player, Attributes.ARMOR, BURNING_ARMOR, "burning_crown_armor", burning ? 10.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        setModifier(player, Attributes.ARMOR_TOUGHNESS, BURNING_TOUGHNESS, "burning_crown_toughness", burning ? 5.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);

        setModifier(player, Attributes.ARMOR, DARKENED_ARMOR, "darkened_crown_armor", 0.0D, AttributeModifier.Operation.ADD_VALUE);
        setModifier(player, Attributes.MOVEMENT_SPEED, DARKENED_SPEED, "darkened_crown_speed", darkened ? 0.12D : 0.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        setModifier(player, Attributes.ARMOR, IRONFORGED_ARMOR, "ironforged_crown_armor", ironforged ? 25.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        setModifier(player, Attributes.ARMOR_TOUGHNESS, IRONFORGED_TOUGHNESS, "ironforged_crown_toughness", ironforged ? 15.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        setModifier(player, Attributes.MOVEMENT_SPEED, IRONFORGED_SPEED, "ironforged_crown_speed", ironforged ? -0.15D : 0.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        setModifier(player, Attributes.MOVEMENT_SPEED, DIMENSIONAL_SPEED, "dimensional_crown_speed", dimensional ? 0.30D : 0.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        setModifier(player, Attributes.ATTACK_DAMAGE, DIMENSIONAL_DAMAGE, "dimensional_crown_damage", dimensional ? 10.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        setModifier(player, Attributes.ATTACK_SPEED, DIMENSIONAL_ATTACK_SPEED, "dimensional_crown_attack_speed", dimensional ? 0.20D : 0.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        setModifier(player, Attributes.MAX_HEALTH, WARRIOR_HEALTH, "warrior_crown_health", warrior ? 25.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        setModifier(player, Attributes.ATTACK_DAMAGE, WARRIOR_DAMAGE, "warrior_crown_damage", warrior ? 12.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        setModifier(player, Attributes.ARMOR, WARRIOR_ARMOR, "warrior_crown_armor", warrior ? -0.30D : 0.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        setModifier(player, Attributes.ARMOR_TOUGHNESS, WARRIOR_TOUGHNESS, "warrior_crown_toughness", warrior ? -0.30D : 0.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        setModifier(player, Attributes.ATTACK_SPEED, WARRIOR_DUEL_ATTACK_SPEED, "warrior_duel_attack_speed",
                warrior && isWarriorDuelActive(player) ? 0.25D : 0.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        setModifier(player, Attributes.MAX_HEALTH, ANGELIC_HEALTH, "angelic_crown_health", angelic ? 20.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        setModifier(player, Attributes.ARMOR_TOUGHNESS, ANGELIC_TOUGHNESS, "angelic_crown_toughness", angelic ? 10.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        setModifier(player, Attributes.MOVEMENT_SPEED, ANGELIC_SPEED, "angelic_crown_speed", angelic ? 0.35D : 0.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static void syncTooltipState(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            syncTooltipStack(player.getInventory().getItem(i), data);
        }
        try {
            CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
                for (var entry : inv.getCurios().values()) {
                    var stacks = entry.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) syncTooltipStack(stacks.getStackInSlot(i), data);
                }
            });
        } catch (RuntimeException ignored) {}
    }

    private static void syncTooltipStack(ItemStack stack, CompoundTag data) {
        if (stack == null || stack.isEmpty()) return;
        if (stack.is(ModItems.BLOODY_CROWN.get())) {
            ItemStackCustomData.update(stack, tag -> {
                tag.putInt(TAG_BLOODY_STACKS, Math.max(0, Math.min(BLOODY_MAX_STACKS, data.getInt(NBT_BLOODY_STACKS))));
                tag.putLong(TAG_BLOODY_READY_AT, data.getLong(NBT_BLOODY_READY));
            });
        } else if (stack.is(ModItems.IRONFORGED_CROWN.get())) {
            ItemStackCustomData.update(stack, tag -> {
                tag.putLong(TAG_IRONFORGED_READY_AT, data.getLong(NBT_IRONFORGED_READY));
            });
        } else if (stack.is(ModItems.WARRIOR_CROWN.get())) {
            ItemStackCustomData.update(stack, tag -> {
                tag.putLong(TAG_WARRIOR_READY_AT, data.getLong(NBT_WARRIOR_READY));
            });
        } else if (stack.is(ModItems.ANGELIC_CROWN.get())) {
            ItemStackCustomData.update(stack, tag -> {
                tag.putLong(TAG_ANGELIC_GUARD_READY_AT, data.getLong(NBT_ANGELIC_GUARD_READY));
                tag.putLong(TAG_ANGELIC_FLIGHT_READY_AT, data.getLong(NBT_ANGELIC_FLIGHT_READY));
            });
        } else {
            ItemStackCustomData.clearIfEmpty(stack);
        }
    }

    private static void tickAngelicFlight(ServerPlayer player, long now) {
        CompoundTag data = player.getPersistentData();
        boolean wearing = isWearingAngelic(player);

        if (wearing && !data.getBoolean(NBT_ANGELIC_SPEED_ACTIVE)) {
            data.putFloat(NBT_ANGELIC_BASE_FLY_SPEED, player.getAbilities().getFlyingSpeed());
            data.putBoolean(NBT_ANGELIC_SPEED_ACTIVE, true);
            player.getAbilities().setFlyingSpeed(player.getAbilities().getFlyingSpeed() * 1.35F);
            player.onUpdateAbilities();
        } else if (!wearing && data.getBoolean(NBT_ANGELIC_SPEED_ACTIVE)) {
            player.getAbilities().setFlyingSpeed(data.getFloat(NBT_ANGELIC_BASE_FLY_SPEED));
            data.remove(NBT_ANGELIC_BASE_FLY_SPEED);
            data.remove(NBT_ANGELIC_SPEED_ACTIVE);
            player.onUpdateAbilities();
        }

        long flightUntil = data.getLong(NBT_ANGELIC_FLIGHT_UNTIL);
        if (flightUntil > now && wearing) {
            // Do not call onUpdateAbilities every tick: doing so repeatedly sends the server's
            // current flying=false flag and can continuously knock the client out of flight.
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
            return;
        }

        if (flightUntil > 0L) data.remove(NBT_ANGELIC_FLIGHT_UNTIL);
        if (data.getBoolean(NBT_ANGELIC_GRANTED_FLIGHT)) {
            boolean hadMayfly = data.getBoolean(NBT_ANGELIC_HAD_MAYFLY);
            if (!hadMayfly && !player.isCreative() && !player.isSpectator()
                    && !CrownLogic.isWearing(player)) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            }
            data.remove(NBT_ANGELIC_GRANTED_FLIGHT);
            data.remove(NBT_ANGELIC_HAD_MAYFLY);
            player.onUpdateAbilities();
        }
    }

    private static void tickAngelicGuardCooldown(ServerPlayer player, long now) {
        CompoundTag data = player.getPersistentData();
        long readyAt = data.getLong(NBT_ANGELIC_GUARD_READY);
        if (!isWearingAngelic(player) || readyAt <= now || player.getHealth() + 0.001F < player.getMaxHealth()) {
            data.remove(NBT_ANGELIC_GUARD_FULL_HEALTH_TICKS);
            return;
        }
        int fullHealthTicks = data.getInt(NBT_ANGELIC_GUARD_FULL_HEALTH_TICKS) + 1;
        if (fullHealthTicks >= 5 * 20) {
            data.putLong(NBT_ANGELIC_GUARD_READY, Math.max(now, readyAt - 3L * 20L));
            data.remove(NBT_ANGELIC_GUARD_FULL_HEALTH_TICKS);
        } else {
            data.putInt(NBT_ANGELIC_GUARD_FULL_HEALTH_TICKS, fullHealthTicks);
        }
    }

    private static void tickOwnedIronGolem(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.hasUUID(NBT_IRONFORGED_GOLEM_UUID) || !(player.level() instanceof ServerLevel level)) return;
        Entity found = level.getEntity(data.getUUID(NBT_IRONFORGED_GOLEM_UUID));
        if (!hasIronforgedGuardianPower(player)) {
            if (found instanceof IronGolem) found.discard();
            data.remove(NBT_IRONFORGED_GOLEM_UUID);
            return;
        }
        if (!(found instanceof IronGolem golem) || !golem.isAlive()) return;
        if (golem.distanceToSqr(player) < 24.0D * 24.0D) return;
        Vec3 look = player.getLookAngle();
        double x = player.getX() - look.x * 2.0D;
        double z = player.getZ() - look.z * 2.0D;
        golem.teleportTo(x, player.getY(), z);
        golem.setDeltaMovement(Vec3.ZERO);
        golem.fallDistance = 0.0F;
    }

    private static void tickWarriorBlockedMotion(ServerPlayer player, long now) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(NBT_WARRIOR_BLOCKED_MOTION_UNTIL)) return;
        long until = data.getLong(NBT_WARRIOR_BLOCKED_MOTION_UNTIL);
        if (now <= until && isWarriorDuelActive(player)) {
            player.setDeltaMovement(
                    data.getDouble(NBT_WARRIOR_BLOCKED_MOTION_X),
                    data.getDouble(NBT_WARRIOR_BLOCKED_MOTION_Y),
                    data.getDouble(NBT_WARRIOR_BLOCKED_MOTION_Z));
            player.hurtMarked = true;
        }
        data.remove(NBT_WARRIOR_BLOCKED_MOTION_UNTIL);
        data.remove(NBT_WARRIOR_BLOCKED_MOTION_X);
        data.remove(NBT_WARRIOR_BLOCKED_MOTION_Y);
        data.remove(NBT_WARRIOR_BLOCKED_MOTION_Z);
    }

    /** Records the wearer's pre-hit motion so damage sources that apply push after hurt() are neutralized. */
    public static void noteWarriorBlockedDamage(ServerPlayer player) {
        if (player == null || !isWarriorDuelActive(player)) return;
        CompoundTag data = player.getPersistentData();
        Vec3 motion = player.getDeltaMovement();
        data.putLong(NBT_WARRIOR_BLOCKED_MOTION_UNTIL, player.level().getGameTime() + 1L);
        data.putDouble(NBT_WARRIOR_BLOCKED_MOTION_X, motion.x);
        data.putDouble(NBT_WARRIOR_BLOCKED_MOTION_Y, motion.y);
        data.putDouble(NBT_WARRIOR_BLOCKED_MOTION_Z, motion.z);
    }

    public static boolean shouldCancelWarriorKnockback(ServerPlayer player) {
        if (player == null || !isWarriorDuelActive(player)) return false;
        return player.getPersistentData().getLong(NBT_WARRIOR_BLOCKED_MOTION_UNTIL) >= player.level().getGameTime();
    }

    public static boolean isWarriorDuelActive(ServerPlayer player) {
        return player != null && (isWearingWarrior(player)
                || GlitchedFusionLogic.has(player, ModItems.WARRIOR_CROWN.get()))
                && player.getPersistentData().getLong(NBT_WARRIOR_DUEL_UNTIL) > player.level().getGameTime();
    }

    public static boolean warriorAllowsDamage(ServerPlayer player, DamageSource source) {
        if (!isWarriorDuelActive(player) || CrownLogic.isForcedDeath(player)) return true;
        if (source == null) return false;
        Entity responsible = source.getEntity();
        Entity direct = source.getDirectEntity();
        if (!(responsible instanceof LivingEntity) || direct != responsible) return false;
        if (source.is(DamageTypeTags.IS_PROJECTILE) || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.is(DamageTypeTags.IS_FIRE)) return false;
        String id = source.getMsgId().toLowerCase(java.util.Locale.ROOT);
        return !id.contains("magic") && !id.contains("spell") && !id.contains("indirect");
    }

    public static boolean isAngelicInvulnerable(ServerPlayer player) {
        return player != null && player.getPersistentData().getLong(NBT_ANGELIC_INVULN_UNTIL) > player.level().getGameTime();
    }

    public static boolean tryAngelicLethalGuard(ServerPlayer player, float incomingDamage) {
        if (player == null || incomingDamage <= 0.0F || !isWearingAngelic(player) || CrownLogic.isForcedDeath(player)) return false;
        if (incomingDamage < player.getHealth()) return false;
        CompoundTag data = player.getPersistentData();
        long now = player.level().getGameTime();
        if (data.getLong(NBT_ANGELIC_GUARD_READY) > now) return false;
        data.putLong(NBT_ANGELIC_GUARD_READY, now + ANGELIC_GUARD_COOLDOWN_TICKS);
        data.putLong(NBT_ANGELIC_INVULN_UNTIL, now + ANGELIC_INVULNERABILITY_TICKS);
        // Hidden safety floor: when Second Chance triggers below 20% health,
        // restore exactly to 20% max health. This also keeps the player clear of
        // the default 10% Glitched execution threshold after activation.
        float safetyFloor = player.getMaxHealth() * 0.20F;
        if (player.getHealth() < safetyFloor) player.setHealth(safetyFloor);
        player.invulnerableTime = ANGELIC_INVULNERABILITY_TICKS;
        player.fallDistance = 0.0F;
        player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
        ModNetworking.CHANNEL.send(com.thecrowns.network.legacy.PacketDistributor.PLAYER.with(() -> player), new AngelicGuardEffectPayload());
        syncTooltipState(player);
        return true;
    }

    public static float protectSetHealth(ServerPlayer player, float requestedHealth) {
        if (player == null || requestedHealth >= player.getHealth() || CrownLogic.isForcedDeath(player)) return requestedHealth;
        if (isWarriorDuelActive(player)) {
            // Normal max-health clamp remains possible when the requested value equals the new maximum.
            if (Math.abs(requestedHealth - player.getMaxHealth()) > 0.001F) return player.getHealth();
        }
        if (requestedHealth <= 0.0F && tryAngelicLethalGuard(player, player.getHealth())) return player.getHealth();
        return requestedHealth;
    }

    public static void activateWarriorDuel(ServerPlayer player) {
        if (player == null || !isWearingWarrior(player)) return;
        long now = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        long readyAt = data.getLong(NBT_WARRIOR_READY);
        if (now < readyAt) {
            player.displayClientMessage(Component.translatable("message.thecrowns.warrior_cooldown",
                    Math.max(1L, (readyAt - now + 19L) / 20L)), true);
            return;
        }
        data.putLong(NBT_WARRIOR_READY, now + WARRIOR_DUEL_COOLDOWN_TICKS);
        data.putLong(NBT_WARRIOR_DUEL_UNTIL, now + WARRIOR_DUEL_DURATION_TICKS);
        data.remove(NBT_WARRIOR_TARGET);
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                MobEffects.DAMAGE_BOOST, WARRIOR_DUEL_DURATION_TICKS, 1, false, true, true));
        player.displayClientMessage(Component.translatable("message.thecrowns.warrior_activated"), true);

        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(32.0D));
        AABB box = player.getBoundingBox().expandTowards(player.getLookAngle().scale(32.0D)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, start, end, box,
                e -> e instanceof LivingEntity living && e != player && isValidWarriorTarget(living), 32.0D * 32.0D);
        if (hit != null && hit.getEntity() instanceof LivingEntity target) {
            data.putUUID(NBT_WARRIOR_TARGET, target.getUUID());
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, WARRIOR_DUEL_DURATION_TICKS, 1), player);
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.WEAKNESS, WARRIOR_DUEL_DURATION_TICKS, 1), player);
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.BLINDNESS, WARRIOR_DUEL_DURATION_TICKS, 1), player);
        }
        syncAttributes(player);
        syncTooltipState(player);
    }

    /** Starts the ordinary 15-second duel state on the nearest hostile struck by the Glitched ray. */
    public static void activateWarriorFusionDuel(ServerPlayer player, LivingEntity target) {
        if (player == null || target == null || !target.isAlive()
                || !GlitchedFusionLogic.has(player, ModItems.WARRIOR_CROWN.get())) return;
        long now = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        data.putLong(NBT_WARRIOR_DUEL_UNTIL, now + WARRIOR_DUEL_DURATION_TICKS);
        data.putUUID(NBT_WARRIOR_TARGET, target.getUUID());
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                MobEffects.DAMAGE_BOOST, WARRIOR_DUEL_DURATION_TICKS, 1, false, true, true));
        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, WARRIOR_DUEL_DURATION_TICKS, 1), player);
        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                MobEffects.WEAKNESS, WARRIOR_DUEL_DURATION_TICKS, 1), player);
        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                MobEffects.BLINDNESS, WARRIOR_DUEL_DURATION_TICKS, 1), player);
        syncAttributes(player);
        player.displayClientMessage(Component.translatable("message.thecrowns.warrior_activated"), true);
    }

    private static boolean isValidWarriorTarget(LivingEntity target) {
        if (target instanceof Player) return true;
        if (target instanceof Enemy) return true;
        return target instanceof NeutralMob;
    }

    public static void activateAngelicFlight(ServerPlayer player) {
        if (player == null || !isWearingAngelic(player)) return;
        long now = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        long readyAt = data.getLong(NBT_ANGELIC_FLIGHT_READY);
        if (now < readyAt) {
            player.displayClientMessage(Component.translatable("message.thecrowns.angelic_flight_cooldown",
                    Math.max(1L, (readyAt - now + 19L) / 20L)), true);
            return;
        }
        data.putLong(NBT_ANGELIC_FLIGHT_READY, now + ANGELIC_FLIGHT_COOLDOWN_TICKS);
        data.putLong(NBT_ANGELIC_FLIGHT_UNTIL, now + ANGELIC_FLIGHT_DURATION_TICKS);
        data.putBoolean(NBT_ANGELIC_HAD_MAYFLY, player.getAbilities().mayfly);
        data.putBoolean(NBT_ANGELIC_GRANTED_FLIGHT, true);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (player.isFallFlying() && !chest.canElytraFly(player)) player.stopFallFlying();
        player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
        player.displayClientMessage(Component.translatable("message.thecrowns.angelic_flight_activated"), true);
        syncTooltipState(player);
    }

    private static void setModifier(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id, String name,
                                    double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier existing = instance.getModifier(id);
        if (amount == 0.0D) {
            if (existing != null) instance.removeModifier(id);
            return;
        }
        if (existing != null && Double.compare(existing.amount(), amount) == 0 && existing.operation() == operation) return;
        if (existing != null) instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, amount, operation));
    }

    private static void removeEffects(ServerPlayer player, Holder<MobEffect>... effects) {
        for (Holder<MobEffect> effect : effects) if (player.hasEffect(effect)) player.removeEffect(effect);
    }

    public static boolean isImmuneToEffect(LivingEntity entity, Holder<MobEffect> effect) {
        if (entity == null || effect == null) return false;
        if (AdvancedCrownLogic.isWearingFrost(entity)
                && (effect == MobEffects.MOVEMENT_SLOWDOWN || effect == MobEffects.DIG_SLOWDOWN)) return true;
        if (isWearingBurning(entity) && (effect == MobEffects.POISON || effect == MobEffects.WITHER
                || effect == MobEffects.MOVEMENT_SLOWDOWN || effect == MobEffects.CONFUSION)) return true;
        return isWearingDarkened(entity) && (effect == MobEffects.DARKNESS || effect == MobEffects.BLINDNESS);
    }

    /** Returns true when the attack should be completely canceled before damage resolution. */
    public static boolean shouldAvoidAttack(ServerPlayer player, DamageSource source) {
        if (player == null || source == null) return false;
        long now = player.level().getGameTime();

        if (isAngelicInvulnerable(player)) return true;
        if (isWearingAngelic(player) && (source.is(DamageTypes.FALL) || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.CRAMMING))) return true;
        if (isWearingDimensional(player) && isVoidDamage(source)) return true;
        if (isWearingDimensional(player) && player.getPersistentData().getBoolean(NBT_DIMENSIONAL_RESCUE_FALL)
                && source.is(DamageTypes.FALL)) return true;
        if (isWearingBurning(player) && source.is(DamageTypeTags.IS_FIRE)) return true;

        if (isWearingDarkened(player)) {
            long readyAt = player.getPersistentData().getLong(NBT_DARKENED_DODGE_READY);
            if (now >= readyAt) {
                player.getPersistentData().putLong(NBT_DARKENED_DODGE_READY, now + DARKENED_DODGE_COOLDOWN_TICKS);
                return true;
            }
        }

        if (hasDimensionalCombatPower(player) && !isEnvironmentalDamage(source) && player.getRandom().nextFloat() < 0.30F) {
            CompoundTag data = player.getPersistentData();
            long readyAt = data.getLong(NBT_DIMENSIONAL_CHARGE_READY);
            if (readyAt > now) data.putLong(NBT_DIMENSIONAL_CHARGE_READY, Math.max(now, readyAt - 20L));
            return true;
        }
        return false;
    }

    private static boolean isVoidDamage(DamageSource source) {
        return source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || source.getMsgId().toLowerCase(java.util.Locale.ROOT).contains("void");
    }

    private static boolean isEnvironmentalDamage(DamageSource source) {
        return source.getEntity() == null && source.getDirectEntity() == null;
    }

    /** Final damage modifiers. Called from LivingDamageEvent after vanilla defenses. */
    public static float modifyFinalDamage(LivingEntity target, DamageSource source, float amount) {
        if (amount <= 0.0F || Float.isNaN(amount)) return amount;
        float result = amount;

        if (target instanceof ServerPlayer victim) {
            if (isLightEmpowered(victim)) {
                result = safeMultiply(result, 0.65D);
            }
            if (isWearingDarkened(victim) || GlitchedFusionLogic.has(victim, ModItems.DARKENED_CROWN.get())) {
                result = safeMultiply(result, 1.50D);
            }
        }

        ServerPlayer attacker = damageOwner(source);
        if (attacker != null && attacker != target) {
            result = applyOutgoingMultipliers(attacker, result, true);
        }
        return Math.max(0.0F, result);
    }

    /** Offensive-only path used by Unleashed RAW_HEALTH, which bypasses LivingDamageEvent. */
    public static float applyOutgoingMultipliers(ServerPlayer attacker, float amount, boolean consumeDimensionalCharge) {
        if (attacker == null || amount <= 0.0F) return amount;
        float result = amount;

        // RAW_HEALTH bypasses later vanilla safety layers, so normalize pathological external values
        // before any Crown multiplier is applied. This cap is technical, not a balance limit.
        if (CrownLogic.isWearingUnleashed(attacker)) result = sanitizeCrownDamage(result);

        if (isLightEmpowered(attacker)) {
            result = safeMultiply(result, 1.50D);
        }
        if (isWearingDarkened(attacker) || GlitchedFusionLogic.has(attacker, ModItems.DARKENED_CROWN.get())) {
            result = safeMultiply(result, 2.0D);
        }
        if (CrownLogic.isWearingUnleashed(attacker)) {
            int fate = CrownLogic.getFatePower(attacker);
            if (fate > 0) {
                double multiplier = CrownLogic.isWearingUnleashedUnleashed(attacker)
                        ? (fate >= 2 ? 32767.0D : 1024.0D)
                        : (fate >= 2 ? com.thecrowns.config.CrownServerConfig.FATE_FULL_DAMAGE_MULTIPLIER.get()
                        : com.thecrowns.config.CrownServerConfig.FATE_HALF_DAMAGE_MULTIPLIER.get());
                result = safeMultiply(result, multiplier);
            }
        }
        if (consumeDimensionalCharge && hasDimensionalCombatPower(attacker)) {
            long now = attacker.level().getGameTime();
            CompoundTag data = attacker.getPersistentData();
            long readyAt = data.getLong(NBT_DIMENSIONAL_CHARGE_READY);
            if (readyAt > 0L && now >= readyAt) {
                result = safeMultiply(result, 3.0D);
                data.putLong(NBT_DIMENSIONAL_CHARGE_READY, now + DIMENSIONAL_CHARGE_TICKS);
            }
        }
        return sanitizeCrownDamage(result);
    }

    /**
     * Keeps Crown-generated damage far below Float.MAX_VALUE (~3.4E38), leaving roughly eight orders
     * of magnitude of headroom for engine/mod conversions while still being astronomically above gameplay values.
     */
    public static float sanitizeCrownDamage(float value) {
        if (Float.isNaN(value) || value <= 0.0F) return 0.0F;
        if (!Float.isFinite(value) || value >= CROWN_DAMAGE_SAFETY_CAP) return CROWN_DAMAGE_SAFETY_CAP;
        return value;
    }

    private static float safeMultiply(float value, double multiplier) {
        float safeValue = sanitizeCrownDamage(value);
        if (safeValue <= 0.0F) return 0.0F;
        if (!Double.isFinite(multiplier) || multiplier <= 0.0D) return 0.0F;
        double product = (double) safeValue * multiplier;
        if (!Double.isFinite(product) || product >= CROWN_DAMAGE_SAFETY_CAP) return CROWN_DAMAGE_SAFETY_CAP;
        if (product <= 0.0D) return 0.0F;
        return (float) product;
    }

    public static void afterDamageResolved(LivingEntity target, DamageSource source, float amount) {
        if (target == null || source == null || amount <= 0.0F) return;
        long now = target.level().getGameTime();

        if (target instanceof ServerPlayer victim) {
            if (isWearingBloody(victim)) {
                CompoundTag data = victim.getPersistentData();
                data.putLong(NBT_BLOODY_LAST_DAMAGE, now);
                long readyAt = data.getLong(NBT_BLOODY_READY);
                if (readyAt > now) data.putLong(NBT_BLOODY_READY, Math.max(now, readyAt - 20L));
            }
            if ((isWearingBurning(victim) || GlitchedFusionLogic.has(victim, ModItems.BURNING_CROWN.get()))
                    && !REFLECTION_GUARD.get()) reflectBurningDamage(victim, source, now);
        }

        ServerPlayer attacker = damageOwner(source);
        if (attacker != null && attacker != target
                && (isWearingBloody(attacker) || GlitchedFusionLogic.has(attacker, ModItems.BLOODY_CROWN.get()))
                && isDirectMeleeDamage(attacker, source)) {
            CompoundTag data = attacker.getPersistentData();
            if (now >= data.getLong(NBT_BLOODY_MELEE_HEAL_READY)) {
                data.putLong(NBT_BLOODY_MELEE_HEAL_READY, now + BLOODY_MELEE_HEAL_COOLDOWN_TICKS);
                attacker.heal(attacker.getMaxHealth() * 0.015F);
            }
        }
    }

    private static void reflectBurningDamage(ServerPlayer wearer, DamageSource source, long now) {
        Entity responsible = source.getEntity();
        if (!(responsible instanceof LivingEntity)) responsible = source.getDirectEntity();
        if (!(responsible instanceof LivingEntity attacker)) return;
        if (attacker == wearer || !attacker.isAlive()) return;

        ReflectionKey key = new ReflectionKey(wearer.getUUID(), attacker.getUUID());
        long readyAt = REFLECTION_COOLDOWNS.getOrDefault(key, Long.MIN_VALUE);
        if (now < readyAt) return;
        REFLECTION_COOLDOWNS.put(key, now + BURNING_REFLECT_COOLDOWN_TICKS);

        double armor = wearer.getAttributeValue(Attributes.ARMOR);
        double toughness = wearer.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float reflectedDamage = (float) (3.0D + armor * 0.4D);
        int fireTicks = Math.max(0, (int) Math.round((4.0D + toughness * 0.5D) * 20.0D));

        REFLECTION_GUARD.set(true);
        try {
            attacker.hurt(wearer.damageSources().thorns(wearer), reflectedDamage);
            attacker.setRemainingFireTicks(Math.max(attacker.getRemainingFireTicks(), fireTicks));
        } finally {
            REFLECTION_GUARD.set(false);
        }
    }

    public static void onLivingKilled(LivingEntity victim, DamageSource source) {
        if (victim == null || source == null) return;
        ServerPlayer attacker = damageOwner(source);
        if (attacker == null || attacker == victim) return;
        long now = attacker.level().getGameTime();
        CompoundTag data = attacker.getPersistentData();

        if (isWearingWarrior(attacker) && data.hasUUID(NBT_WARRIOR_TARGET)
                && data.getUUID(NBT_WARRIOR_TARGET).equals(victim.getUUID())
                && data.getLong(NBT_WARRIOR_DUEL_UNTIL) > now) {
            long readyAt = data.getLong(NBT_WARRIOR_READY);
            if (readyAt > now) data.putLong(NBT_WARRIOR_READY, Math.max(now, readyAt - 30L * 20L));
            data.remove(NBT_WARRIOR_TARGET);
        }

        if (!isWearingBloody(attacker) || !isDirectMeleeDamage(attacker, source) || !isValidBloodyVictim(attacker, victim)) {
            syncTooltipState(attacker);
            return;
        }
        long readyAt = data.getLong(NBT_BLOODY_READY);
        if (readyAt > now) {
            syncTooltipState(attacker);
            return;
        }
        data.putLong(NBT_BLOODY_READY, now + BLOODY_COOLDOWN_TICKS);
        attacker.heal(attacker.getMaxHealth() * 0.20F);
        int stacks = Math.max(0, Math.min(BLOODY_MAX_STACKS, data.getInt(NBT_BLOODY_STACKS)));
        if (stacks < BLOODY_MAX_STACKS) {
            stacks++;
            data.putInt(NBT_BLOODY_STACKS, stacks);
            syncAttributes(attacker);
            attacker.displayClientMessage(Component.translatable("message.thecrowns.bloody_stack", stacks, BLOODY_MAX_STACKS)
                    .withStyle(ChatFormatting.DARK_RED), true);
        }
        syncTooltipState(attacker);
    }

    public static boolean isDirectMeleeDamage(ServerPlayer attacker, DamageSource source) {
        if (attacker == null || source == null) return false;
        String id = source.getMsgId().toLowerCase(java.util.Locale.ROOT);
        return source.getEntity() == attacker && source.getDirectEntity() == attacker
                && !source.is(DamageTypeTags.IS_PROJECTILE) && !source.is(DamageTypeTags.IS_EXPLOSION)
                && !source.is(DamageTypeTags.IS_FIRE)
                && !id.contains("magic") && !id.contains("spell")
                && !id.contains("thorns") && !id.contains("indirect");
    }

    public static boolean shouldBlockWarriorOutgoingDamage(DamageSource source) {
        ServerPlayer attacker = damageOwner(source);
        return attacker != null && isWearingWarrior(attacker) && !CrownLogic.isWearing(attacker)
                && !isDirectMeleeDamage(attacker, source);
    }

    private static boolean isValidBloodyVictim(ServerPlayer attacker, LivingEntity victim) {
        if (victim instanceof Enemy) return true;
        return victim instanceof NeutralMob neutral && neutral.isAngryAt(attacker);
    }

    public static ServerPlayer damageOwner(DamageSource source) {
        if (source == null) return null;
        if (source.getEntity() instanceof ServerPlayer player) return player;
        if (source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile projectile
                && projectile.getOwner() instanceof ServerPlayer player) return player;
        return null;
    }

    public static void noteDarkenedWardenAttack(LivingEntity target, DamageSource source) {
        if (!(target instanceof Warden warden)) return;
        ServerPlayer attacker = damageOwner(source);
        if (attacker == null || !isWearingDarkened(attacker)) return;
        WARDEN_AGGRO.put(new WardenPlayerKey(warden.getUUID(), attacker.getUUID()), attacker.level().getGameTime());
    }

    public static boolean shouldBlockWardenTarget(Warden warden, ServerPlayer player) {
        if (warden == null || player == null || !isWearingDarkened(player)) return false;
        return !isDarkenedWardenAggroActive(warden, player, player.level().getGameTime());
    }

    private static boolean isDarkenedWardenAggroActive(Warden warden, ServerPlayer player, long now) {
        WardenPlayerKey key = new WardenPlayerKey(warden.getUUID(), player.getUUID());
        Long attackedAt = WARDEN_AGGRO.get(key);
        if (attackedAt == null) return false;
        if (now - attackedAt < 40L) return true;
        Optional<LivingEntity> angryAt = warden.getEntityAngryAt();
        if (angryAt.isPresent() && angryAt.get() == player) return true;
        if (warden.getTarget() == player) return true;
        WARDEN_AGGRO.remove(key);
        return false;
    }

    private static void tickDarkenedWardenAffinity(ServerPlayer player, long now) {
        if (!(player.level() instanceof ServerLevel level)) return;
        AABB box = player.getBoundingBox().inflate(96.0D);
        for (Warden warden : level.getEntitiesOfClass(Warden.class, box, Warden::isAlive)) {
            if (isDarkenedWardenAggroActive(warden, player, now)) continue;
            warden.clearAnger(player);
            if (warden.getTarget() == player) warden.setTarget(null);
            try {
                var attackTarget = warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
                if (attackTarget.isPresent() && attackTarget.get() == player) {
                    warden.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                }
                var roarTarget = warden.getBrain().getMemory(MemoryModuleType.ROAR_TARGET);
                if (roarTarget.isPresent() && roarTarget.get() == player) {
                    warden.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
                }
            } catch (RuntimeException ignored) {}
        }
    }

    public static boolean shouldBlockDimensionalAcquisition(Mob mob, LivingEntity newTarget) {
        if (!(newTarget instanceof ServerPlayer player) || !isWearingDimensional(player)) return false;
        if (!(mob instanceof Enemy) && mob.getType().getCategory() != net.minecraft.world.entity.MobCategory.MONSTER) return false;
        AttributeInstance follow = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (follow == null) return false;
        double reducedRange = Math.max(0.0D, follow.getValue() * 0.5D);
        return mob.distanceToSqr(player) > reducedRange * reducedRange;
    }

    public static boolean isOwnedIronGolemTargetBlocked(IronGolem golem, LivingEntity target) {
        if (golem == null || target == null) return false;
        CompoundTag data = golem.getPersistentData();
        if (!data.hasUUID(NBT_IRONFORGED_OWNER)) return false;
        UUID ownerId = data.getUUID(NBT_IRONFORGED_OWNER);
        if (target.getUUID().equals(ownerId)) return true;
        if (!(target instanceof ServerPlayer other) || !(golem.level() instanceof ServerLevel level)) return false;
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null) return false;
        try {
            if (owner.isAlliedTo(other) || other.isAlliedTo(owner)) return true;
        } catch (RuntimeException ignored) {}
        return FTBTeamsCompat.sameTeam(owner, other);
    }

    public static void summonIronGolem(ServerPlayer player) {
        if (player == null || !hasIronforgedGuardianPower(player) || !(player.level() instanceof ServerLevel level)) return;
        CompoundTag data = player.getPersistentData();
        long now = level.getGameTime();
        long readyAt = data.getLong(NBT_IRONFORGED_READY);
        if (now < readyAt) {
            long seconds = Math.max(1L, (readyAt - now + 19L) / 20L);
            player.displayClientMessage(Component.translatable("message.thecrowns.ironforged_cooldown", seconds)
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }

        if (data.hasUUID(NBT_IRONFORGED_GOLEM_UUID)) {
            Entity old = level.getEntity(data.getUUID(NBT_IRONFORGED_GOLEM_UUID));
            if (old instanceof IronGolem) old.discard();
        }

        IronGolem golem = EntityType.IRON_GOLEM.create(level);
        if (golem == null) return;
        Vec3 look = player.getLookAngle();
        golem.moveTo(player.getX() + look.x * 1.5D, player.getY(), player.getZ() + look.z * 1.5D,
                player.getYRot(), 0.0F);
        golem.setPlayerCreated(true);
        golem.getPersistentData().putUUID(NBT_IRONFORGED_OWNER, player.getUUID());

        addToBase(golem, Attributes.MAX_HEALTH, player.getAttributeValue(Attributes.MAX_HEALTH) * 0.5D);
        addToBase(golem, Attributes.ARMOR, player.getAttributeValue(Attributes.ARMOR) * 0.5D);
        addToBase(golem, Attributes.ARMOR_TOUGHNESS, player.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * 0.5D);
        addToBase(golem, Attributes.ATTACK_DAMAGE,
                golem.getAttributeValue(Attributes.ARMOR) * 0.30D
                        + golem.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * 0.60D);
        golem.setHealth(golem.getMaxHealth());
        golem.setPersistenceRequired();

        if (!level.addFreshEntity(golem)) return;
        data.putUUID(NBT_IRONFORGED_GOLEM_UUID, golem.getUUID());
        data.putLong(NBT_IRONFORGED_READY, now + IRONFORGED_COOLDOWN_TICKS);
        syncTooltipState(player);
        level.playSound(null, golem.blockPosition(), SoundEvents.IRON_GOLEM_REPAIR, SoundSource.PLAYERS, 1.0F, 0.8F);
        player.displayClientMessage(Component.translatable("message.thecrowns.ironforged_summoned"), true);
    }

    public static void tickOwnedIronGolemEntity(LivingEntity entity) {
        if (!(entity instanceof IronGolem golem) || !(golem.level() instanceof ServerLevel level)) return;
        CompoundTag gd = golem.getPersistentData();
        if (!gd.hasUUID(NBT_IRONFORGED_OWNER)) return;
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(gd.getUUID(NBT_IRONFORGED_OWNER));
        if (owner == null) return;
        if (!hasIronforgedGuardianPower(owner)) {
            golem.discard();
            owner.getPersistentData().remove(NBT_IRONFORGED_GOLEM_UUID);
            return;
        }
        CompoundTag od = owner.getPersistentData();
        if (od.hasUUID(NBT_IRONFORGED_GOLEM_UUID)
                && !od.getUUID(NBT_IRONFORGED_GOLEM_UUID).equals(golem.getUUID())) {
            golem.discard();
            return;
        }
        // Player-created vanilla golems normally ignore Creepers. Ironforged guardians do not.
        if ((golem.getTarget() == null || !golem.getTarget().isAlive()) && golem.tickCount % 10 == 0) {
            Creeper creeper = level.getNearestEntity(Creeper.class,
                    net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat().range(32.0D),
                    golem, golem.getX(), golem.getY(), golem.getZ(), golem.getBoundingBox().inflate(32.0D));
            if (creeper != null) golem.setTarget(creeper);
        }
    }

    public static void commandOwnedIronGolem(ServerPlayer owner, LivingEntity target) {
        if (owner == null || target == null || target == owner || !(owner.level() instanceof ServerLevel level)) return;
        CompoundTag data = owner.getPersistentData();
        if (!data.hasUUID(NBT_IRONFORGED_GOLEM_UUID)) return;
        Entity entity = level.getEntity(data.getUUID(NBT_IRONFORGED_GOLEM_UUID));
        if (!(entity instanceof IronGolem golem) || !golem.isAlive()) return;
        if (target instanceof ServerPlayer player && isOwnedIronGolemTargetBlocked(golem, player)) return;
        golem.setTarget(target);
    }

    /**
     * Splits the Iron Guardian wearer's raw damage before armor and resistance calculations.
     * The original DamageSource is reused for the guardian's half so vanilla retaliation AI
     * can identify the real attacker. The guardian's per-hit cap is applied separately to the
     * final resolved damage.
     */
    public static float applyIronforgedRawDamageRules(LivingEntity target, DamageSource source, float rawDamage) {
        if (target == null || rawDamage <= 0.0F || Float.isNaN(rawDamage)) return rawDamage;
        if (!(target instanceof ServerPlayer owner) || !hasIronforgedGuardianPower(owner)
                || CrownLogic.isForcedDeath(owner)) return rawDamage;
        IronGolem golem = getOwnedIronGolem(owner);
        if (golem == null || source == null || source.getEntity() == golem || source.getDirectEntity() == golem) {
            return rawDamage;
        }

        float shared = rawDamage * 0.50F;
        golem.invulnerableTime = 0;
        golem.hurt(source, shared);
        Entity responsible = source.getEntity();
        if (!(responsible instanceof LivingEntity)) responsible = source.getDirectEntity();
        if (responsible instanceof LivingEntity attacker && attacker != owner && attacker != golem
                && !(attacker instanceof ServerPlayer player && isOwnedIronGolemTargetBlocked(golem, player))) {
            golem.setTarget(attacker);
        }
        return shared;
    }

    /** Caps an owned Iron Guardian's actual health damage after normal damage mitigation. */
    public static float capIronforgedFinalDamage(LivingEntity target, float finalDamage) {
        if (target == null || finalDamage <= 0.0F || Float.isNaN(finalDamage)) return finalDamage;
        if (target instanceof IronGolem golem && golem.getPersistentData().hasUUID(NBT_IRONFORGED_OWNER)) {
            return Math.min(finalDamage, golem.getMaxHealth() * 0.10F);
        }
        return finalDamage;
    }

    private static IronGolem getOwnedIronGolem(ServerPlayer owner) {
        CompoundTag data = owner.getPersistentData();
        if (!data.hasUUID(NBT_IRONFORGED_GOLEM_UUID) || !(owner.level() instanceof ServerLevel level)) return null;
        Entity entity = level.getEntity(data.getUUID(NBT_IRONFORGED_GOLEM_UUID));
        return entity instanceof IronGolem golem && golem.isAlive() ? golem : null;
    }

    private static void addToBase(LivingEntity entity, Holder<Attribute> attribute, double addition) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(instance.getBaseValue() + Math.max(0.0D, addition));
    }

    /** Used by the RAW_HEALTH route after it directly subtracts body HP. */
    public static void afterAbsoluteDamage(LivingEntity target, DamageSource source, float amount) {
        afterDamageResolved(target, source, amount);
    }

    private record ReflectionKey(UUID wearer, UUID target) {}
    private record WardenPlayerKey(UUID warden, UUID player) {}
}
