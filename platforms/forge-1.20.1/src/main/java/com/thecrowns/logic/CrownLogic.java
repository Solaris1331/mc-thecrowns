package com.thecrowns.logic;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.config.CrownServerConfig;
import com.thecrowns.compat.ForcedMovementCompat;
import com.thecrowns.compat.VestigesCompat;
import com.thecrowns.compat.IceAndFireCompat;
import com.thecrowns.tags.CrownTags;
import com.thecrowns.api.event.CrownAbilityTargetEvent;
import com.thecrowns.api.event.CrownReviveEvent;
import com.thecrowns.registry.ModItems;
import com.thecrowns.network.AnnihilationVisualPayload;
import com.thecrowns.network.ModNetworking;
import com.thecrowns.network.NerfedReviveEffectPayload;
import com.thecrowns.mixin.LivingEntityHealthAccessor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CrownLogic {
    public static final double NULLIFICATION_RADIUS = 200.0D;
    public static final double NERFED_NULLIFICATION_RADIUS = 30.0D;
    public static final double LASER_RANGE = 50.0D;
    public static final double LASER_RADIUS = 2.0D;
    public static final double LASER_PARTICLE_START_DISTANCE = 2.0D;
    public static final int LASER_PARTICLE_DURATION_TICKS = 3; // previous visual window 2 ticks -> +50%
    public static final double ANNIHILATION_RADIUS = 75.0D;
    public static final int ANNIHILATION_WAVE_TICKS = 60;
    public static final int MAX_REVIVES = Integer.MAX_VALUE;
    public static final int UTILITY_EFFECT_REFRESH_TICKS = 4 * 20;
    public static final int UTILITY_EFFECT_DURATION_TICKS = 30 * 20;

    public static final int NERFED_BARRIER_MAX = 30;
    public static final int NERFED_BARRIER_RECHARGE_TICKS = 20 * 10;
    public static final float NERFED_BARRIER_DAMAGE_MULTIPLIER = 0.20F;
    public static final int NERFED_REVIVE_COOLDOWN_TICKS = 20 * 300;
    public static final int NERFED_REVIVE_INVULNERABILITY_TICKS = 20 * 5;
    public static final int NERFED_LASER_COOLDOWN_TICKS = 20 * 150;

    private static final String NBT_REVIVES = "thecrowns_revives";
    private static final String NBT_STORED_MAYFLY = "thecrowns_stored_mayfly";
    private static final String NBT_HAD_CROWN = "thecrowns_had_crown";
    private static final String NBT_FATE = "thecrowns_fate";
    private static final String NBT_AURA = "thecrowns_aura";
    private static final String NBT_NERFED_REVIVE_READY_AT = "thecrowns_nerfed_revive_ready_at";
    private static final String NBT_NERFED_INVULN_UNTIL = "thecrowns_nerfed_invuln_until";
    private static final String NBT_NERFED_CAN_REVIVE = "thecrowns_nerfed_can_revive";
    private static final String NBT_NERFED_SHIELD_ENABLED = "thecrowns_nerfed_shield_enabled";
    private static final String NBT_NERFED_LASER_READY_AT = "thecrowns_nerfed_laser_ready_at";
    private static final String NBT_NERFED_CAN_REMOVE_LAY = "thecrowns_nerfed_can_remove_lay";
    private static final String NBT_NERFED_USE_LASER_COOLDOWN = "thecrowns_nerfed_use_laser_cooldown";
    private static final String NBT_GLITCHED_GATE_WARNING_SHOWN = "thecrowns_gate_warning_shown";
    private static final String NBT_FATE_BIND_UNTIL = "thecrowns_fate_bind_until";
    private static final String NBT_GLITCHED_RAY_REMOVED_FRACTION = "thecrowns_glitched_ray_removed_fraction";
    private static final String NBT_UNLEASHED_FLIGHT_LOCK_INIT = "thecrowns_unleashed_flight_lock_init";
    private static final String NBT_UNLEASHED_FLIGHT_EXPECTED = "thecrowns_unleashed_flight_expected";
    private static final String ITEM_NBT_BARRIER_CHARGES = "GlitchedBarrierCharges";
    private static final String ITEM_NBT_BARRIER_NEXT_RECHARGE = "GlitchedBarrierNextRecharge";
    private static final String ITEM_NBT_LASER_READY_AT = "GlitchedLaserReadyAt";
    private static final String ITEM_NBT_REVIVE_READY_AT = "GlitchedReviveReadyAt";
    private static final String ITEM_NBT_CAN_REVIVE = "GlitchedCanRevive";
    private static final String ITEM_NBT_SHIELD_ENABLED = "GlitchedShieldEnabled";
    private static final String ITEM_NBT_CAN_REMOVE_LAY = "GlitchedCanRemoveLay";
    private static final String ITEM_NBT_USE_LASER_COOLDOWN = "GlitchedUseLaserCooldown";

    private static final UUID MOD_HEALTH = uuid("bonus_health");
    private static final UUID MOD_ARMOR = uuid("bonus_armor");
    private static final UUID MOD_TOUGHNESS = uuid("bonus_toughness");
    private static final UUID MOD_LUCK = uuid("bonus_luck");
    private static final UUID MOD_DAMAGE = uuid("bonus_damage");
    private static final UUID MOD_KB = uuid("knockback_immunity");
    private static final UUID MOD_ENTITY_REACH = uuid("entity_reach");
    private static final UUID MOD_BLOCK_REACH = uuid("block_reach");
    private static final UUID MOD_CRIT_CHANCE = uuid("fate_crit_chance");
    private static final UUID MOD_CRIT_DAMAGE = uuid("fate_crit_damage");
    private static final UUID MOD_ARMOR_PIERCE = uuid("fate_armor_pierce");
    private static final UUID MOD_PROT_SHRED = uuid("fate_prot_shred");
    private static final UUID MOD_FATE_ATTACK_SPEED = uuid("fate_attack_speed");
    private static final UUID MOD_FATE_ATTACK_DAMAGE = uuid("fate_attack_damage");

    private static final Set<UUID> FORCED_DEATHS = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<UUID, AnnihilationWave> ANNIHILATION_WAVES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, LaserVisual> LASER_VISUALS = new ConcurrentHashMap<>();

    private CrownLogic() {
    }

    private static UUID uuid(String path) {
        return UUID.nameUUIDFromBytes((TheCrownsMod.MOD_ID + ":" + path)
                .getBytes(StandardCharsets.UTF_8));
    }

    public static int glitchedBarrierMax() { return CrownServerConfig.GLITCHED_SHIELD_STACKS.get(); }
    public static int glitchedBarrierRechargeTicks() { return CrownServerConfig.GLITCHED_SHIELD_RECHARGE_SECONDS.get() * 20; }
    public static float glitchedBarrierDamageMultiplier() { return CrownServerConfig.GLITCHED_SHIELD_DAMAGE_MULTIPLIER.get().floatValue(); }
    public static int glitchedReviveCooldownTicks() { return CrownServerConfig.GLITCHED_REVIVE_COOLDOWN_SECONDS.get() * 20; }
    public static int glitchedReviveInvulnerabilityTicks() { return CrownServerConfig.GLITCHED_REVIVE_INVULN_SECONDS.get() * 20; }
    public static int glitchedLaserCooldownTicks() { return CrownServerConfig.GLITCHED_REMOVAL_RAY_COOLDOWN_SECONDS.get() * 20; }
    public static double glitchedNullificationRadius() { return CrownServerConfig.GLITCHED_NULLIFICATION_RADIUS.get(); }
    public static double unleashedNullificationRadius(ServerPlayer player) {
        return isWearingUnleashedUnleashed(player) ? NULLIFICATION_RADIUS : CrownServerConfig.UNLEASHED_NULLIFICATION_RADIUS.get();
    }
    public static double laserRange(ServerPlayer player) {
        if (isWearingUnleashedUnleashed(player)) return LASER_RANGE;
        return isWearingConfigurableUnleashed(player) ? CrownServerConfig.UNLEASHED_REMOVAL_RAY_RANGE.get()
                : CrownServerConfig.GLITCHED_REMOVAL_RAY_RANGE.get();
    }
    public static double laserRadius(ServerPlayer player) {
        if (isWearingUnleashedUnleashed(player)) return LASER_RADIUS;
        return isWearingConfigurableUnleashed(player) ? CrownServerConfig.UNLEASHED_REMOVAL_RAY_RADIUS.get()
                : CrownServerConfig.GLITCHED_REMOVAL_RAY_RADIUS.get();
    }
    public static double annihilationRadius(ServerPlayer player) {
        return isWearingUnleashedUnleashed(player) ? ANNIHILATION_RADIUS : CrownServerConfig.UNLEASHED_ANNIHILATION_RADIUS.get();
    }
    public static int glitchedAdvancementRequiredPoints() { return CrownAdvancementGate.requiredPoints(); }
    public static int lootingBonus(LivingEntity entity) {
        if (isWearingUnleashedUnleashed(entity)) return 14;
        if (isWearingConfigurableUnleashed(entity)) return CrownServerConfig.UNLEASHED_BONUS_LOOTING.get();
        return CrownServerConfig.BONUS_LOOTING.get();
    }

    public static boolean isWearing(LivingEntity entity) {
        if (isWearingUnleashed(entity)) return true;
        if (!isWearingGlitched(entity)) return false;
        // Server-side Glitched Crown powers are advancement-gated.  Client-side
        // item detection remains permissive so rendering/keybind state stays stable;
        // the server is authoritative for every actual effect.
        return !(entity instanceof ServerPlayer player) || CrownAdvancementGate.canUseGlitchedCrown(player);
    }

    public static boolean isWearingGlitched(LivingEntity entity) {
        return !getEquippedStack(entity, ModItems.GLITCHED_CROWN.get()).isEmpty();
    }

    /** True for the configurable survival/pack Unleashed Crown only. */
    public static boolean isWearingConfigurableUnleashed(LivingEntity entity) {
        return !getEquippedStack(entity, ModItems.UNLEASHED_CROWN.get()).isEmpty();
    }

    /** Creative/admin immutable full-power variant. */
    public static boolean isWearingUnleashedUnleashed(LivingEntity entity) {
        return ModItems.UNLEASHED_UNLEASHED_CROWN.isPresent()
                && !getEquippedStack(entity, ModItems.UNLEASHED_UNLEASHED_CROWN.get()).isEmpty();
    }

    /** Existing callers treat both Unleashed variants as the Unleashed ruleset. */
    public static boolean isWearingUnleashed(LivingEntity entity) {
        return isWearingUnleashedUnleashed(entity) || isWearingConfigurableUnleashed(entity);
    }

    public static boolean ignoresPackConfig(LivingEntity entity) {
        return isWearingUnleashedUnleashed(entity);
    }

    private static boolean unleashedOption(LivingEntity entity, net.minecraftforge.common.ForgeConfigSpec.BooleanValue option) {
        return isWearingUnleashed(entity) && (ignoresPackConfig(entity) || option.get());
    }

    public static CrownServerConfig.UnleashedDamageMode unleashedDamageMode(ServerPlayer player) {
        if (isWearingUnleashedUnleashed(player)) return CrownServerConfig.UnleashedDamageMode.RAW_HEALTH;
        if (!isWearingConfigurableUnleashed(player) || !CrownServerConfig.UNLEASHED_DEFENSE_BYPASS.get())
            return CrownServerConfig.UnleashedDamageMode.NORMAL;
        return CrownServerConfig.UNLEASHED_DAMAGE_MODE.get();
    }

    public static boolean isUnleashedDefenseBypassActive(ServerPlayer player) {
        return unleashedDamageMode(player) != CrownServerConfig.UnleashedDamageMode.NORMAL;
    }

    public static boolean isUnleashedRawDamageActive(ServerPlayer player) {
        return unleashedDamageMode(player) == CrownServerConfig.UnleashedDamageMode.RAW_HEALTH;
    }

    public static boolean isUnleashedIntegrityActive(ServerPlayer player) {
        return unleashedOption(player, CrownServerConfig.UNLEASHED_INTEGRITY_PROTECTION);
    }

    public static boolean isUnleashedRemovalRayActive(ServerPlayer player) {
        return unleashedOption(player, CrownServerConfig.UNLEASHED_REMOVAL_RAY);
    }

    public static boolean isUnleashedFateAvailable(ServerPlayer player) {
        return unleashedOption(player, CrownServerConfig.UNLEASHED_FATE);
    }

    public static boolean isUnleashedAuraAvailable(ServerPlayer player) {
        return unleashedOption(player, CrownServerConfig.UNLEASHED_OBLIVION_VEIL);
    }

    public static boolean isUnleashedAnnihilationAvailable(ServerPlayer player) {
        return unleashedOption(player, CrownServerConfig.UNLEASHED_ANNIHILATION);
    }

    /** Whether the Unleashed anti-flight-tampering rules should be active. */
    public static boolean isUnleashedFlightLockActive(ServerPlayer player) {
        return player != null && (isWearingUnleashedUnleashed(player)
                || (isWearingConfigurableUnleashed(player) && CrownServerConfig.UNLEASHED_FLIGHT.get()));
    }

    /** The nerfed rules are active only when no Unleashed Crown is also equipped. */
    public static boolean isNerfedCrownActive(LivingEntity entity) {
        if (!isWearingGlitched(entity) || isWearingUnleashed(entity)) return false;
        return !(entity instanceof ServerPlayer player) || CrownAdvancementGate.canUseGlitchedCrown(player);
    }

    public static ItemStack getWornGlitchedCrown(LivingEntity entity) {
        return getEquippedStack(entity, ModItems.GLITCHED_CROWN.get());
    }

    public static ItemStack getWornUnleashedCrown(LivingEntity entity) {
        ItemStack absolute = ModItems.UNLEASHED_UNLEASHED_CROWN.isPresent()
                ? getEquippedStack(entity, ModItems.UNLEASHED_UNLEASHED_CROWN.get()) : ItemStack.EMPTY;
        return absolute.isEmpty() ? getEquippedStack(entity, ModItems.UNLEASHED_CROWN.get()) : absolute;
    }

    private static ItemStack getEquippedStack(LivingEntity entity, Item item) {
        if (entity == null || item == null) return ItemStack.EMPTY;

        try {
            ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (!helmet.isEmpty() && helmet.is(item)) return helmet;
        } catch (RuntimeException ignored) {
            return ItemStack.EMPTY;
        }

        final ItemStack[] found = {ItemStack.EMPTY};
        try {
            CuriosApi.getCuriosInventory(entity).resolve().ifPresent(inv -> {
                for (var entry : inv.getCurios().entrySet()) {
                    var stacks = entry.getValue().getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        if (!stack.isEmpty() && stack.is(item)) {
                            found[0] = stack;
                            return;
                        }
                    }
                    if (!found[0].isEmpty()) return;
                }
            });
        } catch (RuntimeException ignored) {
            return ItemStack.EMPTY;
        }
        return found[0];
    }

    /** True when the player owns either Crown anywhere in vanilla inventory/equipment or Curios. */
    public static boolean ownsCrown(Player player) {
        if (player == null) return false;
        if (isWearing(player)) return true;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && (stack.is(ModItems.GLITCHED_CROWN.get())
                    || stack.is(ModItems.UNLEASHED_CROWN.get())
                    || (ModItems.UNLEASHED_UNLEASHED_CROWN.isPresent() && stack.is(ModItems.UNLEASHED_UNLEASHED_CROWN.get())))) return true;
        }

        try {
            return CuriosApi.getCuriosInventory(player)
                    .resolve()
                    .map(inv -> {
                        for (var entry : inv.getCurios().values()) {
                            var stacks = entry.getStacks();
                            for (int i = 0; i < stacks.getSlots(); i++) {
                                ItemStack stack = stacks.getStackInSlot(i);
                                if (!stack.isEmpty() && (stack.is(ModItems.GLITCHED_CROWN.get())
                                        || stack.is(ModItems.UNLEASHED_CROWN.get())
                                        || (ModItems.UNLEASHED_UNLEASHED_CROWN.isPresent() && stack.is(ModItems.UNLEASHED_UNLEASHED_CROWN.get())))) return true;
                            }
                        }
                        return false;
                    })
                    .orElse(false);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static int getFatePower(ServerPlayer player) {
        if (player == null || !isUnleashedFateAvailable(player)) return 0;
        // Older builds stored this key as a boolean ByteTag. CompoundTag#getInt
        // accepts every numeric tag, so old true/false data migrates as 1/0.
        return Math.max(0, Math.min(2, player.getPersistentData().getInt(NBT_FATE)));
    }

    public static boolean isFateActive(ServerPlayer player) {
        return getFatePower(player) > 0;
    }

    public static boolean isFateFullPower(ServerPlayer player) {
        return getFatePower(player) >= 2;
    }

    public static boolean isAuraActive(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return isWearingUnleashed(entity) && entity.getPersistentData().getBoolean(NBT_AURA);
        }
        return isUnleashedAuraAvailable(player) && entity.getPersistentData().getBoolean(NBT_AURA);
    }

    public static boolean isMovementLocked(Entity entity) {
        // Run on both logical sides. Configurable Unleashed/Glitched can disable
        // this; Unleashed Unleashed always keeps the full lock.
        if (!(entity instanceof Player player)) return false;
        if (ShadowAbyssalLogic.hasShadowHook(player)) return true;
        if (isWearingUnleashedUnleashed(player)) return true;
        if (isWearingConfigurableUnleashed(player)) return CrownServerConfig.UNLEASHED_MOVEMENT_IMMUNITY.get();
        return isNerfedCrownActive(player) && CrownServerConfig.GLITCHED_MOVEMENT_IMMUNITY.get();
    }

    /**
     * Blocks direct velocity rewrites made by known external force systems.
     * Normal player movement is intentionally not intercepted.
     */
    public static boolean isKnownForcedMovementCall() {
        String caller = ForcedMovementCompat.identifyExternalForceCaller();
        if (caller != null && CrownServerConfig.DEBUG_LOGGING.get()) {
            TheCrownsMod.LOGGER.debug("Blocked external Crown movement caller: {}", caller);
        }
        return caller != null;
    }

    public static boolean isGravityChangingEffect(MobEffect effect) {
        if (effect == null) return false;
        try {
            return effect.getAttributeModifiers().containsKey(ForgeMod.ENTITY_GRAVITY.get());
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    public static boolean hasStatusImmunity(LivingEntity entity) {
        if (isWearingUnleashedUnleashed(entity)) return true;
        if (isWearingConfigurableUnleashed(entity)) return CrownServerConfig.UNLEASHED_STATUS_IMMUNITY.get();
        return isNerfedCrownActive(entity) && CrownServerConfig.GLITCHED_STATUS_IMMUNITY.get();
    }

    /** Status effects no Crown wearer is allowed to receive or retain. */
    public static boolean isDisallowedCrownEffect(MobEffect effect) {
        if (effect == null) return false;
        return effect == MobEffects.SLOW_FALLING
                || effect.getCategory() == MobEffectCategory.HARMFUL
                || isGravityChangingEffect(effect);
    }

    /**
     * Forge exposes explicit effect removals separately from natural expiry.
     * Protect positive effects against external removal while still allowing
     * obvious wearer-controlled clears such as drinking milk or /effect clear.
     */
    public static boolean shouldProtectBeneficialEffectRemoval(LivingEntity entity, MobEffect effect) {
        if (!(entity instanceof ServerPlayer player) || entity.level().isClientSide || !isWearing(player)) return false;
        boolean enabled = isWearingUnleashedUnleashed(player)
                || (isWearingConfigurableUnleashed(player) && CrownServerConfig.UNLEASHED_BENEFICIAL_EFFECT_PROTECTION.get())
                || (isNerfedCrownActive(player) && CrownServerConfig.GLITCHED_BENEFICIAL_EFFECT_PROTECTION.get());
        if (!enabled) return false;
        if (effect == null || effect == MobEffects.SLOW_FALLING || effect.getCategory() != MobEffectCategory.BENEFICIAL) return false;
        return !isSelfAuthorizedEffectRemovalCall();
    }

    private static boolean isSelfAuthorizedEffectRemovalCall() {
        for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
            String owner = frame.getClassName();
            if (owner.equals("net.minecraft.world.item.MilkBucketItem")
                    || owner.equals("net.minecraft.server.commands.EffectCommands")
                    || owner.startsWith("com.thecrowns.")) {
                return true;
            }
        }
        return false;
    }

    /** Permanent pre-nerf invulnerability: Unleashed Crown only. */
    public static boolean hasActiveInvulnerability(ServerPlayer player) {
        return !isForcedDeath(player)
                && unleashedOption(player, CrownServerConfig.UNLEASHED_INVULNERABILITY)
                && !isSuppressedByEnemyCrown(player);
    }

    /** Five-second absolute protection granted by the nerfed Crown revive. */
    public static boolean hasNerfedReviveInvulnerability(ServerPlayer player) {
        if (player == null || isForcedDeath(player) || !isNerfedCrownActive(player)) return false;
        long until = player.getPersistentData().getLong(NBT_NERFED_INVULN_UNTIL);
        return player.level().getGameTime() < until && !isInsideEnemyUnleashedNullification(player);
    }

    public static boolean hasAnyDamageInvulnerability(ServerPlayer player) {
        return hasActiveInvulnerability(player) || hasNerfedReviveInvulnerability(player);
    }

    public static boolean isForcedDeath(Entity entity) {
        return entity != null && FORCED_DEATHS.contains(entity.getUUID());
    }

    /** Old Crown-vs-Crown suppression is retained only between Unleashed Crowns. */
    public static boolean isSuppressedByEnemyCrown(ServerPlayer player) {
        return isWearingUnleashed(player) && isInsideEnemyUnleashedNullification(player);
    }

    private static boolean isInsideEnemyUnleashedNullification(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return false;
        for (ServerPlayer other : level.players()) {
            if (other == player || !other.isAlive() || !isWearingUnleashed(other)) continue;
            if (!unleashedOption(other, CrownServerConfig.UNLEASHED_NULLIFICATION_AURA)) continue;
            double radius = isWearingUnleashedUnleashed(other) ? NULLIFICATION_RADIUS
                    : CrownServerConfig.UNLEASHED_NULLIFICATION_RADIUS.get();
            if (player.distanceToSqr(other) > radius * radius) continue;
            if (!isProtectedCrownTarget(player, other)) return true;
        }
        return false;
    }

    /** Per-player Glitched Crown revive switch. Absent NBT means enabled. */
    public static boolean isNerfedReviveEnabled(ServerPlayer player) {
        if (!CrownServerConfig.GLITCHED_REVIVE.get()) return false;
        if (player == null) return true;
        CompoundTag data = player.getPersistentData();
        return !data.contains(NBT_NERFED_CAN_REVIVE) || data.getBoolean(NBT_NERFED_CAN_REVIVE);
    }

    public static void setNerfedReviveEnabled(ServerPlayer player, boolean enabled) {
        if (player == null) return;
        player.getPersistentData().putBoolean(NBT_NERFED_CAN_REVIVE, enabled);
        syncNerfedRuntimeToItem(player);
    }

    /** Per-player Glitched Crown shield switch. Absent NBT means enabled. */
    public static boolean isNerfedShieldEnabled(ServerPlayer player) {
        if (!CrownServerConfig.GLITCHED_SHIELD.get()) return false;
        if (player == null) return true;
        CompoundTag data = player.getPersistentData();
        return !data.contains(NBT_NERFED_SHIELD_ENABLED) || data.getBoolean(NBT_NERFED_SHIELD_ENABLED);
    }

    public static void setNerfedShieldEnabled(ServerPlayer player, boolean enabled) {
        if (player == null) return;
        player.getPersistentData().putBoolean(NBT_NERFED_SHIELD_ENABLED, enabled);
        ItemStack crown = getWornGlitchedCrown(player);
        if (!crown.isEmpty()) {
            crown.getOrCreateTag().putLong(ITEM_NBT_BARRIER_NEXT_RECHARGE,
                    player.level().getGameTime() + glitchedBarrierRechargeTicks());
        }
        syncNerfedRuntimeToItem(player);
    }

    /** Per-player Glitched Crown deletion beam switch. Absent NBT means enabled. */
    public static boolean isNerfedLaserEnabled(ServerPlayer player) {
        if (!CrownServerConfig.GLITCHED_REMOVAL_RAY.get()) return false;
        if (player == null) return true;
        CompoundTag data = player.getPersistentData();
        return !data.contains(NBT_NERFED_CAN_REMOVE_LAY) || data.getBoolean(NBT_NERFED_CAN_REMOVE_LAY);
    }

    public static void setNerfedLaserEnabled(ServerPlayer player, boolean enabled) {
        if (player == null) return;
        player.getPersistentData().putBoolean(NBT_NERFED_CAN_REMOVE_LAY, enabled);
        syncNerfedRuntimeToItem(player);
    }

    /** Whether the Glitched deletion beam obeys its 150-second cooldown. Default true. */
    public static boolean isNerfedLaserCooldownEnabled(ServerPlayer player) {
        if (player == null) return true;
        CompoundTag data = player.getPersistentData();
        return !data.contains(NBT_NERFED_USE_LASER_COOLDOWN)
                || data.getBoolean(NBT_NERFED_USE_LASER_COOLDOWN);
    }

    public static void setNerfedLaserCooldownEnabled(ServerPlayer player, boolean enabled) {
        if (player == null) return;
        player.getPersistentData().putBoolean(NBT_NERFED_USE_LASER_COOLDOWN, enabled);
        syncNerfedRuntimeToItem(player);
    }

    /** Set remaining Glitched Crown revive cooldown in seconds, clamped to 0..300. */
    public static int setNerfedReviveCooldownSeconds(ServerPlayer player, int seconds) {
        if (player == null) return 0;
        int clamped = Math.max(0, Math.min(CrownServerConfig.GLITCHED_REVIVE_COOLDOWN_SECONDS.get(), seconds));
        long now = player.level().getGameTime();
        player.getPersistentData().putLong(NBT_NERFED_REVIVE_READY_AT, now + clamped * 20L);
        syncNerfedRuntimeToItem(player);
        return clamped;
    }

    /** Set remaining Glitched Crown deletion beam cooldown in seconds, clamped to 0..150. */
    public static int setNerfedLaserCooldownSeconds(ServerPlayer player, int seconds) {
        if (player == null) return 0;
        int clamped = Math.max(0, Math.min(CrownServerConfig.GLITCHED_REMOVAL_RAY_COOLDOWN_SECONDS.get(), seconds));
        long readyAt = player.level().getGameTime() + clamped * 20L;
        player.getPersistentData().putLong(NBT_NERFED_LASER_READY_AT, readyAt);
        ItemStack crown = getWornGlitchedCrown(player);
        if (!crown.isEmpty()) crown.getOrCreateTag().putLong(ITEM_NBT_LASER_READY_AT, readyAt);
        return clamped;
    }

    public static void resetGlitchedCooldowns(ServerPlayer player) {
        if (player == null) return;
        CompoundTag data = player.getPersistentData();
        data.remove(NBT_NERFED_REVIVE_READY_AT);
        data.remove(NBT_NERFED_LASER_READY_AT);
        ItemStack crown = getWornGlitchedCrown(player);
        if (!crown.isEmpty()) {
            crown.getOrCreateTag().remove(ITEM_NBT_REVIVE_READY_AT);
            crown.getOrCreateTag().remove(ITEM_NBT_LASER_READY_AT);
        }
        syncNerfedRuntimeToItem(player);
    }

    /** Copy the /crowns switches across an actual player respawn. */
    public static void copyNerfedPlayerSettings(Player original, Player replacement) {
        if (original == null || replacement == null) return;
        CompoundTag src = original.getPersistentData();
        CompoundTag dst = replacement.getPersistentData();
        if (src.contains(NBT_NERFED_CAN_REVIVE)) {
            dst.putBoolean(NBT_NERFED_CAN_REVIVE, src.getBoolean(NBT_NERFED_CAN_REVIVE));
        }
        if (src.contains(NBT_NERFED_SHIELD_ENABLED)) {
            dst.putBoolean(NBT_NERFED_SHIELD_ENABLED, src.getBoolean(NBT_NERFED_SHIELD_ENABLED));
        }
        if (src.contains(NBT_NERFED_REVIVE_READY_AT)) {
            dst.putLong(NBT_NERFED_REVIVE_READY_AT, src.getLong(NBT_NERFED_REVIVE_READY_AT));
        }
        if (src.contains(NBT_NERFED_LASER_READY_AT)) {
            dst.putLong(NBT_NERFED_LASER_READY_AT, src.getLong(NBT_NERFED_LASER_READY_AT));
        }
        if (src.contains(NBT_NERFED_CAN_REMOVE_LAY)) {
            dst.putBoolean(NBT_NERFED_CAN_REMOVE_LAY, src.getBoolean(NBT_NERFED_CAN_REMOVE_LAY));
        }
        if (src.contains(NBT_NERFED_USE_LASER_COOLDOWN)) {
            dst.putBoolean(NBT_NERFED_USE_LASER_COOLDOWN, src.getBoolean(NBT_NERFED_USE_LASER_COOLDOWN));
        }
    }

    public static void toggleFate(ServerPlayer player) {
        if (!isUnleashedFateAvailable(player)) return;
        CompoundTag data = player.getPersistentData();
        int next = (getFatePower(player) + 1) % 3;
        data.putInt(NBT_FATE, next);
        String key = switch (next) {
            case 1 -> "message.thecrowns.fate_half";
            case 2 -> "message.thecrowns.fate_full";
            default -> "message.thecrowns.fate_off";
        };
        player.displayClientMessage(Component.translatable(key), true);
        syncAttributes(player);
    }

    public static void toggleAura(ServerPlayer player) {
        if (!isUnleashedAuraAvailable(player)) return;
        CompoundTag data = player.getPersistentData();
        boolean enabled = !data.getBoolean(NBT_AURA);
        data.putBoolean(NBT_AURA, enabled);
        player.displayClientMessage(Component.translatable(
                enabled ? "message.thecrowns.aura_on" : "message.thecrowns.aura_off"), true);
    }

    public static void tickPlayer(ServerPlayer player) {
        if (isUnleashedIntegrityActive(player)) CrownIntegrity.tick(player);
        tickAnnihilationWave(player);
        tickLaserVisual(player);

        CompoundTag data = player.getPersistentData();
        boolean unleashed = isWearingUnleashed(player);
        boolean rawGlitched = isWearingGlitched(player);
        boolean glitchedGateSatisfied = !rawGlitched || CrownAdvancementGate.canUseGlitchedCrown(player);

        // Warn exactly once per invalid equip session.  Removing the Glitched Crown
        // (or satisfying the advancement gate) arms the warning for the next equip.
        if (rawGlitched && !glitchedGateSatisfied) {
            if (!data.getBoolean(NBT_GLITCHED_GATE_WARNING_SHOWN)) {
                int progress = CrownAdvancementGate.requirementProgress(player);
                player.displayClientMessage(Component.translatable(
                        "message.thecrowns.advancement_gate_not_met", progress, CrownAdvancementGate.requiredPoints())
                        .withStyle(net.minecraft.ChatFormatting.RED), true);
                data.putBoolean(NBT_GLITCHED_GATE_WARNING_SHOWN, true);
            }
        } else {
            data.remove(NBT_GLITCHED_GATE_WARNING_SHOWN);
        }

        boolean nerfed = rawGlitched && !unleashed && glitchedGateSatisfied;
        boolean wearing = unleashed || nerfed;

        if (wearing) {
            if (!data.getBoolean(NBT_HAD_CROWN)) {
                data.putBoolean(NBT_HAD_CROWN, true);
                data.putBoolean(NBT_STORED_MAYFLY, player.getAbilities().mayfly);
            }

            boolean flightEnabled = isWearingUnleashedUnleashed(player)
                    || (unleashed && CrownServerConfig.UNLEASHED_FLIGHT.get())
                    || (nerfed && CrownServerConfig.GLITCHED_FLIGHT.get());
            if (flightEnabled) enforceFlight(player);
            else if (!player.isCreative() && !player.isSpectator()) restoreFlight(player, data);
            if (unleashed && flightEnabled) enforceUnleashedFlightState(player);
            else clearUnleashedFlightLock(data);
            syncAttributes(player);

            if (unleashed) {
                if (isWearingUnleashedUnleashed(player) || CrownServerConfig.UNLEASHED_UTILITY_EFFECTS.get()) {
                    applyUtilityEffects(player);
                }
                if (hasStatusImmunity(player)) clearNegativeEffects(player);

                if (unleashedOption(player, CrownServerConfig.UNLEASHED_INFINITE_REVIVE)
                        && (player.tickCount % 20 == 0 || !data.contains(NBT_REVIVES))) {
                    data.putInt(NBT_REVIVES, MAX_REVIVES);
                }

                if (hasActiveInvulnerability(player)) {
                    if (player.getHealth() < player.getMaxHealth()) player.setHealth(player.getMaxHealth());
                    player.invulnerableTime = 0;
                    player.fallDistance = 0.0F;
                    player.clearFire();
                }

                if (isAuraActive(player)) {
                    player.setInvisible(true);
                    player.setSilent(true);
                    eraseWardenAwareness(player);
                    if (player.tickCount % 10 == 0) eraseMobAwareness(player);
                } else {
                    player.setInvisible(false);
                    player.setSilent(false);
                }

                if (player.tickCount % 5 == 0 && unleashedOption(player, CrownServerConfig.UNLEASHED_NULLIFICATION_AURA)) {
                    stripNearbyHostileInvulnerability(player, unleashedNullificationRadius(player));
                }
            } else {
                // Nerfed Glitched Crown still has no Fated Ruin, Aura of Oblivion,
                // K Annihilation, or permanent invulnerability, but it now shares
                // the Crown's immunity to harmful / gravity-changing status effects.
                data.remove(NBT_FATE);
                data.remove(NBT_AURA);
                player.setInvisible(false);
                player.setSilent(false);
                if (hasStatusImmunity(player)) clearNegativeEffects(player);
                if (CrownServerConfig.GLITCHED_NIGHT_VISION.get()) applyNerfedUtilityEffects(player);
                syncNerfedRuntimeToItem(player);
                if (CrownServerConfig.GLITCHED_SHIELD.get()) tickNerfedBarrier(player);
                if (player.tickCount % 5 == 0) {
                    double radius = glitchedNullificationRadius();
                    boolean nullification = CrownServerConfig.GLITCHED_NULLIFICATION_AURA.get();
                    boolean execution = CrownServerConfig.GLITCHED_EXECUTION_AURA.get();
                    if (nullification || execution) tickNerfedAura(player, radius, nullification, execution);
                }
                if (hasNerfedReviveInvulnerability(player)) {
                    player.invulnerableTime = 0;
                    player.fallDistance = 0.0F;
                    player.clearFire();
                }
            }
        } else if (data.getBoolean(NBT_HAD_CROWN)) {
            restoreFlight(player, data);
            removeAllModifiers(player);
            player.setInvisible(false);
            player.setSilent(false);
            data.remove(NBT_HAD_CROWN);
            data.remove(NBT_STORED_MAYFLY);
            data.remove(NBT_REVIVES);
            data.remove(NBT_FATE);
            data.remove(NBT_AURA);
            clearUnleashedFlightLock(data);
        }
    }

    private static void syncNerfedRuntimeToItem(ServerPlayer player) {
        ItemStack crown = getWornGlitchedCrown(player);
        if (crown.isEmpty()) return;
        CompoundTag tag = crown.getOrCreateTag();
        CompoundTag data = player.getPersistentData();
        tag.putBoolean(ITEM_NBT_CAN_REVIVE, isNerfedReviveEnabled(player));
        tag.putBoolean(ITEM_NBT_SHIELD_ENABLED, isNerfedShieldEnabled(player));
        tag.putBoolean(ITEM_NBT_CAN_REMOVE_LAY, isNerfedLaserEnabled(player));
        tag.putBoolean(ITEM_NBT_USE_LASER_COOLDOWN, isNerfedLaserCooldownEnabled(player));
        tag.putLong(ITEM_NBT_REVIVE_READY_AT, data.getLong(NBT_NERFED_REVIVE_READY_AT));
        if (data.contains(NBT_NERFED_LASER_READY_AT)) {
            tag.putLong(ITEM_NBT_LASER_READY_AT, data.getLong(NBT_NERFED_LASER_READY_AT));
        }
    }

    private static void tickNerfedBarrier(ServerPlayer player) {
        ItemStack crown = getWornGlitchedCrown(player);
        if (crown.isEmpty()) return;
        CompoundTag tag = crown.getOrCreateTag();
        long now = player.level().getGameTime();

        if (!tag.contains(ITEM_NBT_BARRIER_CHARGES)) {
            tag.putInt(ITEM_NBT_BARRIER_CHARGES, glitchedBarrierMax());
            tag.putLong(ITEM_NBT_BARRIER_NEXT_RECHARGE, now + glitchedBarrierRechargeTicks());
            return;
        }

        int charges = getNerfedBarrierCharges(crown);

        // /crowns shieldenable false disables the entire shield mechanic, including
        // passive recharge.  Re-enabling starts a fresh ten-second interval.
        if (!isNerfedShieldEnabled(player)) {
            tag.putInt(ITEM_NBT_BARRIER_CHARGES, charges);
            tag.putLong(ITEM_NBT_BARRIER_NEXT_RECHARGE, now + glitchedBarrierRechargeTicks());
            return;
        }

        long next = tag.contains(ITEM_NBT_BARRIER_NEXT_RECHARGE)
                ? tag.getLong(ITEM_NBT_BARRIER_NEXT_RECHARGE)
                : now + glitchedBarrierRechargeTicks();

        if (charges < glitchedBarrierMax() && next > now
                && GlitchedFusionLogic.doublesBarrierRecharge(player)) {
            next--;
        }

        if (charges < glitchedBarrierMax() && now >= next) {
            long steps = 1L + Math.max(0L, (now - next) / glitchedBarrierRechargeTicks());
            charges = Math.min(glitchedBarrierMax(), charges + (int) Math.min(Integer.MAX_VALUE, steps));
            next += steps * glitchedBarrierRechargeTicks();
        } else if (charges >= glitchedBarrierMax() && now >= next) {
            next = now + glitchedBarrierRechargeTicks();
        }

        tag.putInt(ITEM_NBT_BARRIER_CHARGES, charges);
        tag.putLong(ITEM_NBT_BARRIER_NEXT_RECHARGE, next);
    }

    /** Returns true when one 80%-reduction barrier charge was consumed. */
    public static boolean consumeNerfedBarrierCharge(ServerPlayer player) {
        if (!isNerfedCrownActive(player) || !isNerfedShieldEnabled(player)) return false;
        ItemStack crown = getWornGlitchedCrown(player);
        if (crown.isEmpty()) return false;
        CompoundTag tag = crown.getOrCreateTag();
        long now = player.level().getGameTime();
        int charges = getNerfedBarrierCharges(crown);
        if (charges <= 0) return false;

        boolean wasFull = charges == glitchedBarrierMax();
        tag.putInt(ITEM_NBT_BARRIER_CHARGES, charges - 1);
        if (wasFull || !tag.contains(ITEM_NBT_BARRIER_NEXT_RECHARGE)) {
            tag.putLong(ITEM_NBT_BARRIER_NEXT_RECHARGE, now + glitchedBarrierRechargeTicks());
        }
        return true;
    }

    public static int getNerfedBarrierCharges(ServerPlayer player) {
        ItemStack crown = getWornGlitchedCrown(player);
        return getNerfedBarrierCharges(crown);
    }

    public static int getNerfedBarrierCharges(ItemStack crown) {
        if (crown == null || crown.isEmpty() || !crown.is(ModItems.GLITCHED_CROWN.get())) return 0;
        CompoundTag tag = crown.getTag();
        if (tag == null || !tag.contains(ITEM_NBT_BARRIER_CHARGES)) return glitchedBarrierMax();
        return Math.max(0, Math.min(glitchedBarrierMax(), tag.getInt(ITEM_NBT_BARRIER_CHARGES)));
    }

    /** Set the active worn Glitched Crown shield to an exact clamped value. */
    public static int setNerfedBarrierCharges(ServerPlayer player, int value) {
        ItemStack crown = getWornGlitchedCrown(player);
        if (crown.isEmpty()) return -1;
        int clamped = Math.max(0, Math.min(glitchedBarrierMax(), value));
        CompoundTag tag = crown.getOrCreateTag();
        tag.putInt(ITEM_NBT_BARRIER_CHARGES, clamped);
        tag.putLong(ITEM_NBT_BARRIER_NEXT_RECHARGE,
                player.level().getGameTime() + glitchedBarrierRechargeTicks());
        syncNerfedRuntimeToItem(player);
        return clamped;
    }

    /** Add shield charges to the active worn Glitched Crown, capped at 30. */
    public static int addNerfedBarrierCharges(ServerPlayer player, int amount) {
        ItemStack crown = getWornGlitchedCrown(player);
        if (crown.isEmpty()) return -1;
        return setNerfedBarrierCharges(player, getNerfedBarrierCharges(crown) + Math.max(0, amount));
    }

    public static boolean isNerfedShieldEnabled(ItemStack crown) {
        if (crown == null || crown.isEmpty()) return true;
        CompoundTag tag = crown.getTag();
        return tag == null || !tag.contains(ITEM_NBT_SHIELD_ENABLED) || tag.getBoolean(ITEM_NBT_SHIELD_ENABLED);
    }

    public static boolean isNerfedReviveEnabled(ItemStack crown) {
        if (crown == null || crown.isEmpty()) return true;
        CompoundTag tag = crown.getTag();
        return tag == null || !tag.contains(ITEM_NBT_CAN_REVIVE) || tag.getBoolean(ITEM_NBT_CAN_REVIVE);
    }

    public static long getNerfedReviveCooldownTicks(ItemStack crown, long now) {
        if (crown == null || crown.isEmpty()) return 0L;
        CompoundTag tag = crown.getTag();
        if (tag == null || !tag.contains(ITEM_NBT_REVIVE_READY_AT)) return 0L;
        return Math.max(0L, tag.getLong(ITEM_NBT_REVIVE_READY_AT) - now);
    }

    public static long getNerfedLaserCooldownTicks(ItemStack crown, long now) {
        if (crown == null || crown.isEmpty()) return 0L;
        CompoundTag tag = crown.getTag();
        if (tag == null || !tag.contains(ITEM_NBT_LASER_READY_AT)) return 0L;
        return Math.max(0L, tag.getLong(ITEM_NBT_LASER_READY_AT) - now);
    }

    public static boolean isNerfedLaserEnabled(ItemStack crown) {
        if (crown == null || crown.isEmpty()) return true;
        CompoundTag tag = crown.getTag();
        return tag == null || !tag.contains(ITEM_NBT_CAN_REMOVE_LAY) || tag.getBoolean(ITEM_NBT_CAN_REMOVE_LAY);
    }

    public static boolean isNerfedLaserCooldownEnabled(ItemStack crown) {
        if (crown == null || crown.isEmpty()) return true;
        CompoundTag tag = crown.getTag();
        return tag == null || !tag.contains(ITEM_NBT_USE_LASER_COOLDOWN)
                || tag.getBoolean(ITEM_NBT_USE_LASER_COOLDOWN);
    }

    private static void eraseMobAwareness(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        AABB area = player.getBoundingBox().inflate(256.0D);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, area, Mob::isAlive)) {
            if (mob.getTarget() == player) {
                mob.setTarget(null);
                mob.getNavigation().stop();
            }
        }
    }

    /** Warden tracks anger independently of Mob#getTarget; clear that state every aura tick. */
    private static void eraseWardenAwareness(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        AABB area = player.getBoundingBox().inflate(256.0D);
        for (Warden warden : level.getEntitiesOfClass(Warden.class, area, Warden::isAlive)) {
            warden.clearAnger(player);
            if (warden.getTarget() == player) warden.setTarget(null);
            warden.getNavigation().stop();
            try {
                warden.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                warden.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
                warden.getBrain().eraseMemory(MemoryModuleType.DISTURBANCE_LOCATION);
            } catch (RuntimeException ignored) {
            }
        }
    }

    private static void applyUtilityEffects(ServerPlayer player) {
        if (player.tickCount % UTILITY_EFFECT_REFRESH_TICKS != 0) return;
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, UTILITY_EFFECT_DURATION_TICKS, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, UTILITY_EFFECT_DURATION_TICKS, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 0, false, false, false));
    }

    private static void applyNerfedUtilityEffects(ServerPlayer player) {
        if (player.tickCount % UTILITY_EFFECT_REFRESH_TICKS != 0) return;
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, UTILITY_EFFECT_DURATION_TICKS, 0, false, false, false));
    }

    private static void clearNegativeEffects(ServerPlayer player) {
        List<MobEffect> toRemove = new ArrayList<>();
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (isDisallowedCrownEffect(instance.getEffect())) {
                toRemove.add(instance.getEffect());
            }
        }
        toRemove.forEach(player::removeEffect);
    }

    private static void enforceFlight(ServerPlayer player) {
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    /**
     * Commits the player's own flight-toggle packet for Unleashed / U² after vanilla handling.
     * This records the player's intent as the authoritative expected state, while the tick/sync
     * guards are reserved for later unauthorized external changes.
     */
    public static boolean applyAuthorizedPlayerFlightToggle(ServerPlayer player, boolean desiredFlying) {
        if (!isUnleashedFlightLockActive(player)) return false;
        CompoundTag data = player.getPersistentData();
        data.putBoolean(NBT_UNLEASHED_FLIGHT_LOCK_INIT, true);
        data.putBoolean(NBT_UNLEASHED_FLIGHT_EXPECTED, desiredFlying);

        boolean changed = !player.getAbilities().mayfly || player.getAbilities().flying != desiredFlying;
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = desiredFlying;
        if (changed) player.onUpdateAbilities();
        return true;
    }

    private static void enforceUnleashedFlightState(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(NBT_UNLEASHED_FLIGHT_LOCK_INIT)) {
            data.putBoolean(NBT_UNLEASHED_FLIGHT_LOCK_INIT, true);
            data.putBoolean(NBT_UNLEASHED_FLIGHT_EXPECTED, player.getAbilities().flying);
        }

        boolean expected = data.getBoolean(NBT_UNLEASHED_FLIGHT_EXPECTED);
        boolean changed = false;
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            changed = true;
        }
        // Only restore unauthorized/external changes. The player's own requested state
        // is written into NBT by applyAuthorizedPlayerFlightToggle before this runs.
        if (player.getAbilities().flying != expected) {
            player.getAbilities().flying = expected;
            changed = true;
        }
        if (changed) player.onUpdateAbilities();
    }

    /** Called from ServerPlayer#onUpdateAbilities before an external state is synced. */
    public static void guardUnleashedFlightBeforeSync(ServerPlayer player) {
        if (!isUnleashedFlightLockActive(player)) return;
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(NBT_UNLEASHED_FLIGHT_LOCK_INIT)) {
            data.putBoolean(NBT_UNLEASHED_FLIGHT_LOCK_INIT, true);
            data.putBoolean(NBT_UNLEASHED_FLIGHT_EXPECTED, player.getAbilities().flying);
        }
        player.getAbilities().mayfly = true;
        boolean expected = data.getBoolean(NBT_UNLEASHED_FLIGHT_EXPECTED);
        if (player.getAbilities().flying != expected) player.getAbilities().flying = expected;
    }

    private static void clearUnleashedFlightLock(CompoundTag data) {
        data.remove(NBT_UNLEASHED_FLIGHT_LOCK_INIT);
        data.remove(NBT_UNLEASHED_FLIGHT_EXPECTED);
    }

    private static void restoreFlight(ServerPlayer player, CompoundTag data) {
        if (player.isCreative() || player.isSpectator()) return;
        boolean oldMayfly = data.getBoolean(NBT_STORED_MAYFLY);
        player.getAbilities().mayfly = oldMayfly;
        if (!oldMayfly) player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }

    private static void syncAttributes(ServerPlayer player) {
        boolean fixed = isWearingUnleashedUnleashed(player);
        boolean unleashed = isWearingConfigurableUnleashed(player);
        apply(player, Attributes.MAX_HEALTH, MOD_HEALTH, "bonus_health", fixed ? 160.0D : unleashed
                ? CrownServerConfig.UNLEASHED_BONUS_MAX_HEALTH.get() : CrownServerConfig.BONUS_MAX_HEALTH.get());
        apply(player, Attributes.ARMOR, MOD_ARMOR, "bonus_armor", fixed ? 80.0D : unleashed
                ? CrownServerConfig.UNLEASHED_BONUS_ARMOR.get() : CrownServerConfig.BONUS_ARMOR.get());
        apply(player, Attributes.ARMOR_TOUGHNESS, MOD_TOUGHNESS, "bonus_toughness", fixed ? 80.0D : unleashed
                ? CrownServerConfig.UNLEASHED_BONUS_TOUGHNESS.get() : CrownServerConfig.BONUS_TOUGHNESS.get());
        apply(player, Attributes.LUCK, MOD_LUCK, "bonus_luck", fixed ? 14.0D : unleashed
                ? CrownServerConfig.UNLEASHED_BONUS_LUCK.get() : CrownServerConfig.BONUS_LUCK.get());
        apply(player, Attributes.ATTACK_DAMAGE, MOD_DAMAGE, "bonus_damage", fixed ? 80.0D : unleashed
                ? CrownServerConfig.UNLEASHED_BONUS_ATTACK_DAMAGE.get() : CrownServerConfig.BONUS_ATTACK_DAMAGE.get());
        apply(player, Attributes.KNOCKBACK_RESISTANCE, MOD_KB, "knockback_immunity", fixed ? 2.0D : unleashed
                ? CrownServerConfig.UNLEASHED_BONUS_KNOCKBACK_RESISTANCE.get() : CrownServerConfig.BONUS_KNOCKBACK_RESISTANCE.get());
        double reach = fixed ? 7.0D : unleashed ? CrownServerConfig.UNLEASHED_BONUS_INTERACTION_REACH.get()
                : CrownServerConfig.BONUS_INTERACTION_REACH.get();
        apply(player, ForgeMod.ENTITY_REACH.get(), MOD_ENTITY_REACH, "entity_reach", reach);
        apply(player, ForgeMod.BLOCK_REACH.get(), MOD_BLOCK_REACH, "block_reach", reach);

        Attribute critChance = findAttribute("attributeslib:crit_chance", "apothic_attributes:crit_chance");
        Attribute critDamage = findAttribute("attributeslib:crit_damage", "apothic_attributes:crit_damage");
        Attribute armorPierce = findAttribute("attributeslib:armor_pierce", "apothic_attributes:armor_pierce");
        Attribute protShred = findAttribute("attributeslib:prot_shred", "apothic_attributes:prot_shred");

        // The old Apothic overcrit route is deliberately disabled. End Power now
        // uses a Crown-owned final damage multiplier in NewCrownLogic.
        remove(player, critChance, MOD_CRIT_CHANCE);
        remove(player, critDamage, MOD_CRIT_DAMAGE);

        int fatePower = getFatePower(player);
        if (fatePower > 0) {
            apply(player, armorPierce, MOD_ARMOR_PIERCE, "fate_armor_pierce", fixed ? 100.0D : CrownServerConfig.FATE_ARMOR_PIERCE.get());
            apply(player, protShred, MOD_PROT_SHRED, "fate_prot_shred", fixed ? 1.0D : CrownServerConfig.FATE_PROTECTION_SHRED.get());
            double speed = fixed ? (fatePower >= 2 ? 10.0D : 5.0D)
                    : (fatePower >= 2 ? CrownServerConfig.FATE_FULL_ATTACK_SPEED.get() : CrownServerConfig.FATE_HALF_ATTACK_SPEED.get());
            double damage = fixed ? (fatePower >= 2 ? 100.0D : 50.0D)
                    : (fatePower >= 2 ? CrownServerConfig.FATE_FULL_ATTACK_DAMAGE.get() : CrownServerConfig.FATE_HALF_ATTACK_DAMAGE.get());
            apply(player, Attributes.ATTACK_SPEED, MOD_FATE_ATTACK_SPEED, "fate_attack_speed", speed);
            apply(player, Attributes.ATTACK_DAMAGE, MOD_FATE_ATTACK_DAMAGE, "fate_attack_damage", damage);
        } else {
            remove(player, armorPierce, MOD_ARMOR_PIERCE);
            remove(player, protShred, MOD_PROT_SHRED);
            remove(player, Attributes.ATTACK_SPEED, MOD_FATE_ATTACK_SPEED);
            remove(player, Attributes.ATTACK_DAMAGE, MOD_FATE_ATTACK_DAMAGE);
        }
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static Attribute findAttribute(String... keys) {
        for (String key : keys) {
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(key));
            if (attribute != null) return attribute;
        }
        return null;
    }

    private static void apply(ServerPlayer player, Attribute attribute, UUID id,
                              String name, double amount) {
        if (attribute == null) return;
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;

        AttributeModifier existing = instance.getModifier(id);
        if (existing != null && Double.compare(existing.getAmount(), amount) == 0
                && existing.getOperation() == AttributeModifier.Operation.ADDITION) {
            return;
        }

        if (existing != null) instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(
                id,
                TheCrownsMod.MOD_ID + ":" + name,
                amount,
                AttributeModifier.Operation.ADDITION
        ));
    }

    private static void removeAllModifiers(ServerPlayer player) {
        remove(player, Attributes.MAX_HEALTH, MOD_HEALTH);
        remove(player, Attributes.ARMOR, MOD_ARMOR);
        remove(player, Attributes.ARMOR_TOUGHNESS, MOD_TOUGHNESS);
        remove(player, Attributes.LUCK, MOD_LUCK);
        remove(player, Attributes.ATTACK_DAMAGE, MOD_DAMAGE);
        remove(player, Attributes.KNOCKBACK_RESISTANCE, MOD_KB);
        remove(player, ForgeMod.ENTITY_REACH.get(), MOD_ENTITY_REACH);
        remove(player, ForgeMod.BLOCK_REACH.get(), MOD_BLOCK_REACH);
        remove(player, findAttribute("attributeslib:crit_chance", "apothic_attributes:crit_chance"), MOD_CRIT_CHANCE);
        remove(player, findAttribute("attributeslib:crit_damage", "apothic_attributes:crit_damage"), MOD_CRIT_DAMAGE);
        remove(player, findAttribute("attributeslib:armor_pierce", "apothic_attributes:armor_pierce"), MOD_ARMOR_PIERCE);
        remove(player, findAttribute("attributeslib:prot_shred", "apothic_attributes:prot_shred"), MOD_PROT_SHRED);
        remove(player, Attributes.ATTACK_SPEED, MOD_FATE_ATTACK_SPEED);
        remove(player, Attributes.ATTACK_DAMAGE, MOD_FATE_ATTACK_DAMAGE);
    }

    private static void remove(ServerPlayer player, Attribute attribute, UUID modifier) {
        if (attribute == null) return;
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) instance.removeModifier(modifier);
    }

    public static boolean tryRevive(ServerPlayer player) {
        if (player == null || isForcedDeath(player)) return false;

        if (isWearingUnleashed(player)) {
            if (!unleashedOption(player, CrownServerConfig.UNLEASHED_INFINITE_REVIVE)
                    || isSuppressedByEnemyCrown(player)) return false;
            CrownReviveEvent reviveEvent = new CrownReviveEvent(player, CrownReviveEvent.Kind.UNLEASHED);
            if (MinecraftForge.EVENT_BUS.post(reviveEvent)) return false;
            CompoundTag data = player.getPersistentData();
            int remaining = data.contains(NBT_REVIVES) ? data.getInt(NBT_REVIVES) : MAX_REVIVES;
            if (remaining <= 0) return false;
            data.putInt(NBT_REVIVES, remaining - 1);
            reviveNow(player, 20);
            return true;
        }

        if (!isNerfedCrownActive(player) || !isNerfedReviveEnabled(player)) return false;
        if (MinecraftForge.EVENT_BUS.post(new CrownReviveEvent(player, CrownReviveEvent.Kind.GLITCHED))) return false;
        CompoundTag data = player.getPersistentData();
        long now = player.level().getGameTime();
        long readyAt = data.getLong(NBT_NERFED_REVIVE_READY_AT);
        if (now < readyAt) return false;

        data.putLong(NBT_NERFED_REVIVE_READY_AT, now + GlitchedFusionLogic.glitchedReviveCooldownTicks(player));
        data.putLong(NBT_NERFED_INVULN_UNTIL, now + glitchedReviveInvulnerabilityTicks());
        // Successful Glitched Crown revival instantly restores +15 shield charges,
        // never exceeding the normal 30-stack cap.
        addNerfedBarrierCharges(player, (glitchedBarrierMax() + 1) / 2);
        syncNerfedRuntimeToItem(player);
        playNerfedReviveEffect(player);
        reviveNow(player, glitchedReviveInvulnerabilityTicks());
        GlitchedFusionLogic.onGlitchedRevive(player);
        return true;
    }

    private static void playNerfedReviveEffect(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new NerfedReviveEffectPayload());
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + player.getBbHeight() * 0.5D, player.getZ(),
                30, 0.45D, 0.65D, 0.45D, 0.10D);
        level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE,
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void reviveNow(ServerPlayer player, int vanillaInvulnerableTicks) {
        player.setHealth(player.getMaxHealth());
        player.deathTime = 0;
        player.invulnerableTime = vanillaInvulnerableTicks;
        player.fallDistance = 0.0F;
        player.clearFire();
        player.setDeltaMovement(Vec3.ZERO);
    }

    /**
     * Normal Glitched Crown nullification: only removes the ordinary post-hit
     * hurt-resistance window. It deliberately does NOT clear an entity's
     * invulnerable flag and does not bypass Entity#isInvulnerableTo.
     */
    private static void tickNerfedAura(ServerPlayer wearer, double radius, boolean nullification, boolean execution) {
        if (!(wearer.level() instanceof ServerLevel level) || !isNerfedCrownActive(wearer)) return;
        AABB area = wearer.getBoundingBox().inflate(radius);
        double radiusSquared = radius * radius;
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, area, entity ->
                entity != wearer && !entity.isRemoved() && entity.distanceToSqr(wearer) <= radiusSquared);

        // The two Glitched passives share their broad-phase query. Their individual
        // target gates remain separate, so gameplay results are unchanged.
        if (nullification) {
            for (LivingEntity target : nearby) {
                if (!target.isAlive() || !isHostile(target)
                        || !canAffectCrownTarget(wearer, target, CrownAbilityTargetEvent.Ability.NULLIFICATION)) continue;
                target.invulnerableTime = 0;
                target.hurtTime = 0;
            }
        }
        if (!execution) return;
        double threshold = CrownServerConfig.GLITCHED_EXECUTION_THRESHOLD.get();
        for (LivingEntity target : nearby) {
            if (!canAffectCrownTarget(wearer, target, CrownAbilityTargetEvent.Ability.EXECUTION)) continue;
            // Angelic Crown's triggered four-second invulnerability explicitly
            // resists the normal Glitched Crown execution aura.
            if (target instanceof ServerPlayer player && NewCrownLogic.isAngelicInvulnerable(player)) continue;
            float hp = rawHealth(target);
            float max = target.getMaxHealth();
            if (hp > 0.0F && max > 0.0F && hp < max * threshold) {
                forceDeathFromCrown(wearer, target, wearer.damageSources().playerAttack(wearer));
            }
        }
    }

    /** Unleashed/U² absolute nullification keeps the historical invulnerability bypass. */
    private static void stripNearbyHostileInvulnerability(ServerPlayer wearer, double radius) {
        if (!(wearer.level() instanceof ServerLevel level)) return;
        AABB area = wearer.getBoundingBox().inflate(radius);
        double radiusSquared = radius * radius;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, entity ->
                entity.isAlive() && entity != wearer && isHostile(entity)
                        && entity.distanceToSqr(wearer) <= radiusSquared)) {
            if (!canAffectCrownTarget(wearer, target, CrownAbilityTargetEvent.Ability.NULLIFICATION)) continue;
            target.setInvulnerable(false);
            target.invulnerableTime = 0;
            target.hurtTime = 0;
        }
    }

    public static boolean shouldStripInvulnerability(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !(entity.level() instanceof ServerLevel level)) return false;

        // The old Crown-vs-Crown rule survives only for Unleashed vs Unleashed.
        if (living instanceof ServerPlayer player && isWearingUnleashed(player)) {
            return isSuppressedByEnemyCrown(player);
        }

        // The nerfed Crown never nullifies another player Crown.
        if (!isHostile(living)) return false;

        for (ServerPlayer wearer : level.players()) {
            if (!wearer.isAlive() || !isWearingUnleashed(wearer)
                    || !unleashedOption(wearer, CrownServerConfig.UNLEASHED_NULLIFICATION_AURA)) continue;
            double radius = unleashedNullificationRadius(wearer);
            if (radius > 0.0D && wearer.distanceToSqr(living) <= radius * radius
                    && canAffectCrownTarget(wearer, living, CrownAbilityTargetEvent.Ability.NULLIFICATION)) return true;
        }
        return false;
    }

    /** Shared friendly/tamed protection used by every Crown target selector. */
    public static boolean isProtectedCrownTarget(ServerPlayer wearer, Entity target) {
        return CrownTargeting.isProtectedTarget(wearer, target);
    }

    /** Public pack/API target gate used by offensive Crown abilities. */
    public static boolean canAffectCrownTarget(ServerPlayer wearer, Entity target,
                                                CrownAbilityTargetEvent.Ability ability) {
        if (wearer == null || target == null || isProtectedCrownTarget(wearer, target)) return false;
        try {
            if (target.getType().builtInRegistryHolder().is(CrownTags.PROTECTED_FROM_CROWN_OFFENSE)) return false;
            if (ability == CrownAbilityTargetEvent.Ability.REMOVAL_RAY
                    && target.getType().builtInRegistryHolder().is(CrownTags.PROTECTED_FROM_REMOVAL_RAY)) return false;
            if (ability == CrownAbilityTargetEvent.Ability.ANNIHILATION
                    && target.getType().builtInRegistryHolder().is(CrownTags.PROTECTED_FROM_ANNIHILATION)) return false;
            if (ability == CrownAbilityTargetEvent.Ability.EXECUTION
                    && target.getType().builtInRegistryHolder().is(CrownTags.PROTECTED_FROM_EXECUTION)) return false;
        } catch (RuntimeException ignored) {}
        return !MinecraftForge.EVENT_BUS.post(new CrownAbilityTargetEvent(wearer, target, ability));
    }

    /** True for vanilla or modded entities which are currently tamed/owned. */
    public static boolean isTamedCrownProtected(Entity entity) {
        return CrownTargeting.isTamedOrOwned(entity);
    }

    private static boolean isHostile(LivingEntity entity) {
        return entity instanceof Enemy || (entity instanceof Mob mob && mob.getTarget() instanceof ServerPlayer);
    }

    /** Five-second absolute bind applied by Fated Ruin after a successful damage event. */
    public static void applyFateBind(LivingEntity target) {
        if (target == null || target.level().isClientSide || target.isRemoved()) return;
        target.getPersistentData().putLong(NBT_FATE_BIND_UNTIL, target.level().getGameTime() + 100L);
        target.setDeltaMovement(Vec3.ZERO);
        target.fallDistance = 0.0F;
        if (target instanceof Mob mob) mob.getNavigation().stop();
    }

    public static boolean isFateBound(Entity entity) {
        if (!(entity instanceof LivingEntity living) || living.level().isClientSide) return false;
        long until = living.getPersistentData().getLong(NBT_FATE_BIND_UNTIL);
        return until > living.level().getGameTime();
    }

    public static void tickFateBind(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide) return;
        CompoundTag data = entity.getPersistentData();
        long until = data.getLong(NBT_FATE_BIND_UNTIL);
        if (until <= 0L) return;
        if (entity.level().getGameTime() >= until) {
            data.remove(NBT_FATE_BIND_UNTIL);
            return;
        }
        entity.setDeltaMovement(Vec3.ZERO);
        entity.fallDistance = 0.0F;
        if (entity instanceof Mob mob) mob.getNavigation().stop();
    }

    public static ServerPlayer crownDamageOwner(net.minecraft.world.damagesource.DamageSource source) {
        if (source == null) return null;
        if (source.getEntity() instanceof ServerPlayer player) return player;
        if (source.getDirectEntity() instanceof Projectile projectile
                && projectile.getOwner() instanceof ServerPlayer player) return player;
        return null;
    }

    /** Direct vanilla health-data access, bypassing setHealth() mixins such as VP Cruel shield. */
    public static float rawHealth(LivingEntity target) {
        return target.getEntityData().get(LivingEntityHealthAccessor.thecrowns$getDataHealth());
    }

    public static void rawSetHealth(LivingEntity target, float value) {
        float clamped = Math.max(0.0F, Math.min(value, target.getMaxHealth()));
        target.getEntityData().set(LivingEntityHealthAccessor.thecrowns$getDataHealth(), clamped);
    }

    /** Records cumulative non-player body-health loss caused by the Glitched disassembly ray. */
    private static void recordGlitchedRayHealthLoss(LivingEntity target, float removedHealth) {
        if (target == null || target instanceof Player || removedHealth <= 0.0F || target.getMaxHealth() <= 0.0F) return;
        CompoundTag data = target.getPersistentData();
        double removedFraction = removedHealth / target.getMaxHealth();
        double total = Math.min(1.0D, Math.max(0.0D, data.getDouble(NBT_GLITCHED_RAY_REMOVED_FRACTION))
                + Math.max(0.0D, removedFraction));
        data.putDouble(NBT_GLITCHED_RAY_REMOVED_FRACTION, total);
        enforceGlitchedRayHealingCap(target);
    }

    private static float glitchedRayHealingCeiling(LivingEntity entity) {
        if (entity == null || entity instanceof Player) return Float.POSITIVE_INFINITY;
        double removed = Math.min(1.0D, Math.max(0.0D,
                entity.getPersistentData().getDouble(NBT_GLITCHED_RAY_REMOVED_FRACTION)));
        return (float) Math.max(0.0D, entity.getMaxHealth() * (1.0D - removed));
    }

    public static float clampSetHealth(LivingEntity entity, float requestedHealth) {
        return Math.min(requestedHealth, glitchedRayHealingCeiling(entity));
    }

    public static float clampHealingAmount(LivingEntity entity, float amount) {
        if (entity == null || amount <= 0.0F || entity instanceof Player) return amount;
        return Math.max(0.0F, Math.min(amount, glitchedRayHealingCeiling(entity) - rawHealth(entity)));
    }

    public static void enforceGlitchedRayHealingCap(LivingEntity entity) {
        if (entity == null || entity instanceof Player) return;
        float ceiling = glitchedRayHealingCeiling(entity);
        if (rawHealth(entity) > ceiling) rawSetHealth(entity, ceiling);
    }

    /**
     * Applies the LivingHurt-resolved amount directly to body health for damage
     * caused by an Unleashed Crown wearer. The call site deliberately runs after
     * offensive LivingHurt modifiers (critical hits, etc.) but before vanilla
     * armor/resistance/absorption and Forge LivingDamage, which is where VP's
     * Shield/OverShield and Cruel damage cap are implemented.
     */
    public static boolean applyResolvedAbsoluteDamage(LivingEntity target,
                                                       net.minecraft.world.damagesource.DamageSource source,
                                                       float amount) {
        if (target == null || target.isRemoved() || amount <= 0.0F || Float.isNaN(amount)) return false;
        ServerPlayer player = crownDamageOwner(source);
        if (player == null || !isUnleashedRawDamageActive(player) || target == player) return false;
        if (!canAffectCrownTarget(player, target, CrownAbilityTargetEvent.Ability.ABSOLUTE_DAMAGE)) return false;
        if (target instanceof ServerPlayer victim) {
            if (!NewCrownLogic.warriorAllowsDamage(victim, source)) return true;
        }

        target.setInvulnerable(false);
        target.invulnerableTime = 0;
        target.hurtTime = 0;
        target.hurtDuration = 0;

        float safeAmount = NewCrownLogic.sanitizeCrownDamage(amount);
        if (safeAmount <= 0.0F) return true;
        float boostedAmount = NewCrownLogic.applyOutgoingMultipliers(player, safeAmount, true);
        boostedAmount = NewCrownLogic.capIronforgedFinalDamage(target, boostedAmount);
        if (target instanceof ServerPlayer victim && NewCrownLogic.tryAngelicLethalGuard(victim, boostedAmount)) return true;
        float before = rawHealth(target);
        float next = Math.max(0.0F, before - boostedAmount);
        rawSetHealth(target, next);
        NewCrownLogic.afterAbsoluteDamage(target, source, Math.max(0.0F, before - next));
        if (isFateActive(player) && next > 0.0F
                && canAffectCrownTarget(player, target, CrownAbilityTargetEvent.Ability.FATE_BIND)) applyFateBind(target);
        if (next <= 0.0F) {
            // VP also has a death-stage guard keyed from its shield data. The
            // hit has already bypassed those shields, so do not let their stale
            // values veto the subsequent normal death path. Other revival
            // systems are intentionally left to LivingEntity#hurt.
            VestigesCompat.clearDeathGuards(target);
        }
        return true;
    }

    public static void forceDeathFromCrown(ServerPlayer wearer, LivingEntity target,
                                           net.minecraft.world.damagesource.DamageSource source) {
        if (target == null || target.isRemoved()) return;
        FORCED_DEATHS.add(target.getUUID());
        try {
            target.setInvulnerable(false);
            target.invulnerableTime = 0;
            target.hurtTime = 0;
            target.hurtDuration = 0;
            VestigesCompat.clearDeathGuards(target);
            rawSetHealth(target, 0.0F);
            target.die(source);

            // A direct die() mixin can still cancel before Forge's death hook. If
            // that happened, Forced Death wins; corpse-style Ice and Fire dragons
            // are exempt from physical removal so their harvestable body remains.
            if (!IceAndFireCompat.preservesDragonCorpse(target)
                    && !((com.thecrowns.mixin.LivingEntityDeadAccessor) target).thecrowns$isDead()) {
                target.remove(Entity.RemovalReason.KILLED);
            }
        } finally {
            FORCED_DEATHS.remove(target.getUUID());
        }
    }

    public static void fireLaser(ServerPlayer shooter) {
        boolean unleashed = isWearingUnleashed(shooter);
        boolean nerfed = isNerfedCrownActive(shooter);
        if ((!unleashed && !nerfed) || !(shooter.level() instanceof ServerLevel level)) return;
        if (unleashed && !isUnleashedRemovalRayActive(shooter)) return;
        if (nerfed && !beginNerfedLaserCooldown(shooter)) return;

        double range = laserRange(shooter);
        double radius = laserRadius(shooter);
        Vec3 direction = shooter.getLookAngle().normalize();
        Vec3 start = shooter.getEyePosition();
        Vec3 end = start.add(direction.scale(range));
        AABB broadPhase = new AABB(start, end).inflate(radius);

        Set<Integer> handled = new HashSet<>();
        List<LivingEntity> livingTargets = new ArrayList<>();
        for (Entity raw : level.getEntities(shooter, broadPhase, Entity::isAlive)) {
            Entity target = resolveMultipartParent(raw);
            if (target == shooter || !handled.add(target.getId())) continue;
            if (!canAffectCrownTarget(shooter, target, CrownAbilityTargetEvent.Ability.REMOVAL_RAY)) continue;
            if (target.getBoundingBox().inflate(radius).clip(start, end).isEmpty()) continue;

            if (target instanceof ShulkerBullet bullet) {
                if (unleashed) {
                    bullet.setSilent(true);
                    bullet.kill();
                }
                continue;
            }

            if (target instanceof LivingEntity living) {
                livingTargets.add(living);
            }
        }

        if (nerfed && GlitchedFusionLogic.has(shooter, ModItems.WARRIOR_CROWN.get())) {
            LivingEntity duelTarget = livingTargets.stream()
                    .filter(target -> GlitchedFusionLogic.isHostileTo(shooter, target))
                    .min(java.util.Comparator.comparingDouble(target -> shooter.distanceToSqr(target)))
                    .orElse(null);
            if (duelTarget != null) NewCrownLogic.activateWarriorFusionDuel(shooter, duelTarget);
        }
        for (LivingEntity living : livingTargets) {
            if (unleashed) applyLaserDamageSequence(shooter, living);
            else applyNerfedLaserDamage(shooter, living);
        }

        emitLaserParticles(level, start, direction, range);
        LASER_VISUALS.put(shooter.getUUID(), new LaserVisual(level.dimension(), start, direction, range, 1));
        level.playSound(null, shooter.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
                SoundSource.PLAYERS, 2.0F, unleashed ? 0.55F : 0.85F);
        shooter.displayClientMessage(Component.translatable("message.thecrowns.laser_activated"), true);
    }

    private static void emitLaserParticles(ServerLevel level, Vec3 start, Vec3 direction, double range) {
        // Damage begins at the eye, but visuals start two blocks ahead so first-person view stays clear.
        for (double distance = LASER_PARTICLE_START_DISTANCE; distance <= range; distance += 2.0D) {
            Vec3 point = start.add(direction.scale(distance));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void tickLaserVisual(ServerPlayer shooter) {
        LaserVisual visual = LASER_VISUALS.get(shooter.getUUID());
        if (visual == null) return;
        if (!(shooter.level() instanceof ServerLevel level) || !level.dimension().equals(visual.dimension())) {
            LASER_VISUALS.remove(shooter.getUUID());
            return;
        }
        if (visual.age() >= LASER_PARTICLE_DURATION_TICKS) {
            LASER_VISUALS.remove(shooter.getUUID());
            return;
        }
        emitLaserParticles(level, visual.start(), visual.direction(), visual.range());
        LASER_VISUALS.put(shooter.getUUID(), new LaserVisual(visual.dimension(), visual.start(), visual.direction(), visual.range(), visual.age() + 1));
    }

    private static boolean beginNerfedLaserCooldown(ServerPlayer shooter) {
        if (!isNerfedLaserEnabled(shooter)) {
            shooter.displayClientMessage(Component.translatable("message.thecrowns.laser_disabled"), true);
            return false;
        }
        ItemStack crown = getWornGlitchedCrown(shooter);
        if (crown.isEmpty()) return false;
        if (!isNerfedLaserCooldownEnabled(shooter)) return true;
        CompoundTag data = shooter.getPersistentData();
        CompoundTag tag = crown.getOrCreateTag();
        long now = shooter.level().getGameTime();

        // Migrate the old item-local cooldown into the new per-player runtime value once.
        if (!data.contains(NBT_NERFED_LASER_READY_AT) && tag.contains(ITEM_NBT_LASER_READY_AT)) {
            data.putLong(NBT_NERFED_LASER_READY_AT, tag.getLong(ITEM_NBT_LASER_READY_AT));
        }
        long readyAt = data.getLong(NBT_NERFED_LASER_READY_AT);
        if (now < readyAt) {
            long seconds = Math.max(1L, (readyAt - now + 19L) / 20L);
            shooter.displayClientMessage(Component.translatable(
                    "message.thecrowns.laser_cooldown", seconds), true);
            return false;
        }
        readyAt = now + GlitchedFusionLogic.glitchedLaserCooldownTicks(shooter);
        data.putLong(NBT_NERFED_LASER_READY_AT, readyAt);
        tag.putLong(ITEM_NBT_LASER_READY_AT, readyAt);
        return true;
    }

    private static void applyNerfedLaserDamage(ServerPlayer shooter, LivingEntity target) {
        if (target instanceof ServerPlayer victim && NewCrownLogic.isWarriorDuelActive(victim)) return;
        double fraction = target instanceof Player ? CrownServerConfig.GLITCHED_RAY_PLAYER_FRACTION.get()
                : (isBoss(target) ? CrownServerConfig.GLITCHED_RAY_BOSS_FRACTION.get()
                : CrownServerConfig.GLITCHED_RAY_NORMAL_FRACTION.get());
        float amount = (float) (target.getMaxHealth() * fraction);
        amount = NewCrownLogic.applyOutgoingMultipliers(shooter, amount, true);
        amount = AdvancedCrownLogic.applyCursedOutgoingPenalty(shooter, amount);
        float next = Math.max(0.0F, rawHealth(target) - amount);

        target.invulnerableTime = 0;
        target.hurtTime = 0;
        target.hurtDuration = 0;
        // Bypass VP Cruel's setHealth() interception and apply loss to body HP directly.
        float applied = Math.max(0.0F, rawHealth(target) - next);
        rawSetHealth(target, next);
        recordGlitchedRayHealthLoss(target, applied);
        if (applied > 0.0F) {
            AdvancedCrownLogic.onDamageResolved(target, shooter.damageSources().playerAttack(shooter), applied);
        }

        if (next <= 0.0F) {
            FORCED_DEATHS.add(target.getUUID());
            try {
                target.die(shooter.damageSources().playerAttack(shooter));
                // Ice and Fire dragons use their still-present dead entity as the
                // harvestable corpse.  Do not erase it after the normal death path.
                if (!IceAndFireCompat.preservesDragonCorpse(target) && target.isAlive()) {
                    // VP can cancel die() while OverShield remains.  Only escalate
                    // while the target is still alive; a confirmed death may leave
                    // behind a legitimate corpse entity that must not be discarded.
                    target.kill();
                }
            } finally {
                FORCED_DEATHS.remove(target.getUUID());
            }
        }
    }

    /**
     * Ice and Fire dragons intentionally remain in-world as harvestable corpses
     * after death.  Crown lethal beams must not follow their normal die() call
     * with kill()/discard(), or the corpse entity is removed immediately.
     * Registry-id detection keeps Ice and Fire an optional dependency.
     */
    public static boolean isBoss(LivingEntity target) {
        if (target.getType() == net.minecraft.world.entity.EntityType.ENDER_DRAGON
                || target.getType() == net.minecraft.world.entity.EntityType.WITHER) return true;

        try {
            if (target.getType().builtInRegistryHolder().tags().anyMatch(tag -> {
                String path = tag.location().getPath().toLowerCase(Locale.ROOT);
                return path.equals("boss") || path.equals("bosses") || path.contains("boss");
            })) return true;
        } catch (RuntimeException ignored) {
        }

        for (Class<?> type = target.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (BossEvent.class.isAssignableFrom(field.getType())) return true;
            }
        }
        return false;
    }

    /**
     * Absolute retaliation used by the crown integrity guard.
     *
     * This intentionally reuses the same FORCED_DEATHS bypass as the deletion
     * beam so another Glitched Crown, resurrection hooks, or ordinary
     * invulnerability cannot turn the retaliation into a normal survivable hit.
     */
    public static void forceKillInterferer(Entity entity) {
        if (entity == null || entity.isRemoved()) return;

        if (!(entity instanceof LivingEntity living)) {
            entity.discard();
            return;
        }

        FORCED_DEATHS.add(living.getUUID());
        try {
            living.setInvulnerable(false);
            living.invulnerableTime = 0;
            living.hurtTime = 0;
            living.hurtDuration = 0;
            living.kill();
            if (!living.isRemoved() && living.isAlive()) {
                living.setHealth(0.0F);
                living.kill();
            }
            if (!living.isRemoved() && living.isAlive()) living.discard();
        } finally {
            FORCED_DEATHS.remove(living.getUUID());
        }
    }

    /**
     * Deletion beam 1.0.7 sequence.  No direct die() and no discard() are used.
     * The normal max-health hit is followed by a direct health collapse and kill().
     */
    private static void applyLaserDamageSequence(ServerPlayer shooter, LivingEntity target) {
        var source = shooter.damageSources().playerAttack(shooter);
        FORCED_DEATHS.add(target.getUUID());
        try {
            target.setSilent(true);
            target.setInvulnerable(false);
            target.invulnerableTime = 0;
            target.hurtTime = 0;
            target.hurtDuration = 0;

            if (target instanceof ServerPlayer victim && NewCrownLogic.isWarriorDuelActive(victim)) return;

            if (isWearingUnleashedUnleashed(shooter)) {
                // U² intentionally keeps the historical immutable 1600% hit followed by collapse.
                float afterFixed = Math.max(0.0F, rawHealth(target) - target.getMaxHealth() * 16.0F);
                rawSetHealth(target, afterFixed);
                if (afterFixed > 0.0F) rawSetHealth(target, 0.0F);
            } else {
                // Normal Unleashed now directly collapses remaining body health.
                rawSetHealth(target, 0.0F);
            }
            if (rawHealth(target) > 0.0F) return;

            // Ice and Fire dragons must enter their normal corpse state instead
            // of being physically removed from the world.
            if (IceAndFireCompat.preservesDragonCorpse(target)) {
                target.die(source);
            } else {
                // Direct remove path bypasses VP's shield-aware setHealth()/die hooks.
                // If kill()/a modded death hook has already established a dead state,
                // stop there instead of erasing a corpse with discard().
                target.kill();
            }
        } finally {
            FORCED_DEATHS.remove(target.getUUID());
        }
    }

    /**
     * K ability: starts a three-second expanding annihilation shell.  The lethal
     * front advances with the visible golden surface instead of resolving the
     * entire 75-block sphere in the activation tick.
     */
    public static void annihilate(ServerPlayer shooter) {
        if (!isUnleashedAnnihilationAvailable(shooter) || !(shooter.level() instanceof ServerLevel level)) return;

        double maxRadius = annihilationRadius(shooter);
        Vec3 origin = shooter.position().add(0.0D, shooter.getBbHeight() * 0.5D, 0.0D);
        ANNIHILATION_WAVES.put(shooter.getUUID(),
                new AnnihilationWave(level.dimension(), origin, maxRadius, 0, new HashSet<>()));
        ModNetworking.CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension),
                new AnnihilationVisualPayload(origin.x, origin.y, origin.z, maxRadius));
        level.playSound(null, shooter.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 3.0F, 0.45F);
        shooter.displayClientMessage(Component.translatable("message.thecrowns.annihilation_activated"), true);
    }

    private static void applyAnnihilationLivingSequence(ServerPlayer shooter, LivingEntity target) {
        if (target instanceof ServerPlayer victim && NewCrownLogic.isWarriorDuelActive(victim)) return;
        FORCED_DEATHS.add(target.getUUID());
        try {
            target.setSilent(true);
            target.setInvulnerable(false);
            target.invulnerableTime = 0;
            target.hurtTime = 0;
            target.hurtDuration = 0;
            rawSetHealth(target, 0.0F);
            target.kill();
            if (!target.isRemoved()) target.discard();
        } finally {
            FORCED_DEATHS.remove(target.getUUID());
        }
    }

    private static boolean isAnnihilationProtected(Entity entity) {
        if (entity instanceof Player
                || entity instanceof ArmorStand
                || entity instanceof HangingEntity
                || entity instanceof AbstractVillager
                || entity instanceof IronGolem
                || entity instanceof SnowGolem
                || entity instanceof Allay
                || entity instanceof ItemEntity
                || entity instanceof ExperienceOrb
                || entity instanceof Boat
                || entity instanceof AbstractMinecart) {
            return true;
        }
        if (isTamedCrownProtected(entity)) return true;

        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (key == null) return false;
        String path = key.getPath();
        return path.equals("interaction")
                || path.equals("block_display")
                || path.equals("item_display")
                || path.equals("text_display")
                || path.equals("marker")
                || path.equals("leash_knot")
                || path.endsWith("_minecart");
    }

    private static void tickAnnihilationWave(ServerPlayer player) {
        AnnihilationWave wave = ANNIHILATION_WAVES.get(player.getUUID());
        if (wave == null) return;
        if (!isUnleashedAnnihilationAvailable(player)) {
            ANNIHILATION_WAVES.remove(player.getUUID());
            return;
        }
        if (!(player.level() instanceof ServerLevel level) || !level.dimension().equals(wave.dimension())) {
            ANNIHILATION_WAVES.remove(player.getUUID());
            return;
        }

        int age = wave.age() + 1;
        double previousRadius = wave.maxRadius() * (age - 1) / (double) ANNIHILATION_WAVE_TICKS;
        double radius = wave.maxRadius() * age / (double) ANNIHILATION_WAVE_TICKS;
        AABB scan = new AABB(wave.origin(), wave.origin()).inflate(Math.min(wave.maxRadius(), radius + 8.0D));

        for (Entity raw : level.getEntities(player, scan, entity -> !entity.isRemoved())) {
            Entity target = resolveMultipartParent(raw);
            if (target == player || target instanceof Player || isAnnihilationProtected(target)) continue;
            if (!canAffectCrownTarget(player, target, CrownAbilityTargetEvent.Ability.ANNIHILATION)) continue;
            if (wave.handled().contains(target.getUUID())) continue;
            if (!annihilationShellIntersects(wave.origin(), previousRadius, radius, target.getBoundingBox())) continue;
            wave.handled().add(target.getUUID());

            if (target instanceof LivingEntity living) {
                applyAnnihilationLivingSequence(player, living);
            } else {
                target.setSilent(true);
                target.kill();
                if (!target.isRemoved()) target.discard();
            }
        }

        if (age >= ANNIHILATION_WAVE_TICKS) {
            ANNIHILATION_WAVES.remove(player.getUUID());
        } else {
            ANNIHILATION_WAVES.put(player.getUUID(),
                    new AnnihilationWave(wave.dimension(), wave.origin(), wave.maxRadius(), age, wave.handled()));
        }
    }

    private static boolean annihilationShellIntersects(Vec3 origin, double previousRadius,
                                                        double radius, AABB box) {
        double dx = axisDistance(origin.x, box.minX, box.maxX);
        double dy = axisDistance(origin.y, box.minY, box.maxY);
        double dz = axisDistance(origin.z, box.minZ, box.maxZ);
        double minDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double fx = Math.max(Math.abs(box.minX - origin.x), Math.abs(box.maxX - origin.x));
        double fy = Math.max(Math.abs(box.minY - origin.y), Math.abs(box.maxY - origin.y));
        double fz = Math.max(Math.abs(box.minZ - origin.z), Math.abs(box.maxZ - origin.z));
        double maxDistance = Math.sqrt(fx * fx + fy * fy + fz * fz);
        return radius >= minDistance && previousRadius <= maxDistance;
    }

    private static double axisDistance(double point, double min, double max) {
        if (point < min) return min - point;
        if (point > max) return point - max;
        return 0.0D;
    }

    private record LaserVisual(ResourceKey<Level> dimension, Vec3 start, Vec3 direction, double range, int age) {
    }

    private record AnnihilationWave(ResourceKey<Level> dimension, Vec3 origin, double maxRadius, int age, Set<UUID> handled) {
    }

    private static Entity resolveMultipartParent(Entity entity) {
        try {
            Method method = entity.getClass().getMethod("getParent");
            Object result = method.invoke(entity);
            if (result instanceof Entity parent) return parent;
        } catch (ReflectiveOperationException ignored) {
        }
        return entity;
    }
}
