package com.thecrowns.logic;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.registry.ModItems;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.BlockPos;

/** Runtime abilities for the Shadow and Abyssal Tier I Crowns. */
public final class ShadowAbyssalLogic {
    private static final int SHADOW_STEALTH_DELAY = 8 * 20;
    private static final int ABYSSAL_COMBAT_DELAY = 3 * 20;
    private static final int ABYSSAL_DEEP_REGEN_DELAY = 10 * 20;

    private static final String NBT_SHADOW_LAST_COMBAT = "thecrowns_shadow_last_combat";
    private static final String NBT_SHADOW_ACTIVE = "thecrowns_shadow_active";
    private static final String NBT_SHADOW_AMBUSH_TICK = "thecrowns_shadow_ambush_tick";
    private static final String NBT_ABYSSAL_LAST_COMBAT = "thecrowns_abyssal_last_combat";
    private static final String NBT_ABYSSAL_WATER_TICKS = "thecrowns_abyssal_water_ticks";
    private static final String NBT_ABYSSAL_WATER_BONUS = "thecrowns_abyssal_water_bonus";
    private static final String NBT_ABYSSAL_WATER_SCAN = "thecrowns_abyssal_water_scan";

    private static final UUID SHADOW_DAMAGE = uuid("shadow_crown_damage");
    private static final UUID SHADOW_ATTACK_SPEED = uuid("shadow_crown_attack_speed");
    private static final UUID ABYSSAL_HEALTH = uuid("abyssal_crown_health");
    private static final UUID ABYSSAL_UNDERWATER_SPEED = uuid("abyssal_crown_underwater_speed");
    private static final UUID ABYSSAL_WATER_DAMAGE = uuid("abyssal_crown_water_damage");
    private static final UUID ABYSSAL_WATER_ARMOR = uuid("abyssal_crown_water_armor");
    private static final UUID ABYSSAL_WATER_SPEED = uuid("abyssal_crown_water_speed");

    private ShadowAbyssalLogic() {}

    private static UUID uuid(String path) {
        return UUID.nameUUIDFromBytes((TheCrownsMod.MOD_ID + ":" + path).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isWearingShadow(LivingEntity entity) {
        return NewCrownLogic.isWearing(entity, ModItems.SHADOW_CROWN.get());
    }

    public static boolean isWearingAbyssal(LivingEntity entity) {
        return NewCrownLogic.isWearing(entity, ModItems.ABYSSAL_CROWN.get());
    }

    private static boolean hasShadowStealth(ServerPlayer player) {
        return isWearingShadow(player) || GlitchedFusionLogic.has(player, ModItems.SHADOW_CROWN.get());
    }

    private static boolean hasAbyssalCore(ServerPlayer player) {
        return isWearingAbyssal(player) || GlitchedFusionLogic.has(player, ModItems.ABYSSAL_CROWN.get());
    }

    /** The Shadow Hook applies before stealth and also blocks external velocity systems. */
    public static boolean hasShadowHook(Entity entity) {
        return entity instanceof LivingEntity living && isWearingShadow(living);
    }

    public static boolean isShadowInvisible(ServerPlayer player) {
        return player != null && player.getPersistentData().getBoolean(NBT_SHADOW_ACTIVE) && hasShadowStealth(player);
    }

    public static void tickPlayer(ServerPlayer player) {
        long now = player.level().getGameTime();
        tickShadow(player, now);
        tickAbyssal(player, now);
        syncAttributes(player);
    }

    private static void tickShadow(ServerPlayer player, long now) {
        CompoundTag data = player.getPersistentData();
        if (!hasShadowStealth(player)) {
            boolean wasActive = data.getBoolean(NBT_SHADOW_ACTIVE);
            data.remove(NBT_SHADOW_ACTIVE);
            if (wasActive && !CrownLogic.isAuraActive(player)) {
                player.setInvisible(false);
                player.setSilent(false);
            }
            data.remove(NBT_SHADOW_LAST_COMBAT);
            data.remove(NBT_SHADOW_AMBUSH_TICK);
            return;
        }
        if (!data.contains(NBT_SHADOW_LAST_COMBAT)) data.putLong(NBT_SHADOW_LAST_COMBAT, now);
        if (now - data.getLong(NBT_SHADOW_LAST_COMBAT) >= SHADOW_STEALTH_DELAY) {
            data.putBoolean(NBT_SHADOW_ACTIVE, true);
            player.setInvisible(true);
            player.setSilent(true);
            if (now % 10L == 0L) clearNearbyTargets(player);
        }
    }

    private static void tickAbyssal(ServerPlayer player, long now) {
        CompoundTag data = player.getPersistentData();
        boolean ownCrown = isWearingAbyssal(player);
        if (!hasAbyssalCore(player)) {
            data.remove(NBT_ABYSSAL_LAST_COMBAT);
            data.remove(NBT_ABYSSAL_WATER_TICKS);
            data.remove(NBT_ABYSSAL_WATER_BONUS);
            data.remove(NBT_ABYSSAL_WATER_SCAN);
            return;
        }

        if (!data.contains(NBT_ABYSSAL_LAST_COMBAT)) data.putLong(NBT_ABYSSAL_LAST_COMBAT, now);
        if (ownCrown) player.removeEffect(MobEffects.DIG_SLOWDOWN);
        boolean inWater = player.isInWaterOrBubble();
        if (inWater) {
            player.setAirSupply(player.getMaxAirSupply());
            data.putInt(NBT_ABYSSAL_WATER_TICKS, data.getInt(NBT_ABYSSAL_WATER_TICKS) + 1);
            long lastCombat = data.getLong(NBT_ABYSSAL_LAST_COMBAT);
            if (now - lastCombat >= ABYSSAL_COMBAT_DELAY && now % 20L == 0L) {
                float healing = data.getInt(NBT_ABYSSAL_WATER_TICKS) >= ABYSSAL_DEEP_REGEN_DELAY ? 5.0F : 1.0F;
                if (player.getHealth() < player.getMaxHealth()) player.heal(healing);
            }
        } else {
            data.putInt(NBT_ABYSSAL_WATER_TICKS, 0);
        }

        if (ownCrown && now >= data.getLong(NBT_ABYSSAL_WATER_SCAN)) {
            data.putDouble(NBT_ABYSSAL_WATER_BONUS, waterBonus(player));
            data.putLong(NBT_ABYSSAL_WATER_SCAN, now + 20L);
        } else if (!ownCrown) {
            data.remove(NBT_ABYSSAL_WATER_BONUS);
            data.remove(NBT_ABYSSAL_WATER_SCAN);
        }
    }

    private static void syncAttributes(ServerPlayer player) {
        boolean shadow = isWearingShadow(player);
        boolean abyssal = isWearingAbyssal(player);
        double waterBonus = abyssal ? player.getPersistentData().getDouble(NBT_ABYSSAL_WATER_BONUS) : 0.0D;
        setModifier(player, Attributes.ATTACK_DAMAGE, SHADOW_DAMAGE, "shadow_crown_damage", shadow ? 7.0D : 0.0D, AttributeModifier.Operation.ADDITION);
        setModifier(player, Attributes.ATTACK_SPEED, SHADOW_ATTACK_SPEED, "shadow_crown_attack_speed", shadow ? 0.07D : 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setModifier(player, Attributes.MAX_HEALTH, ABYSSAL_HEALTH, "abyssal_crown_health", abyssal ? 10.0D : 0.0D, AttributeModifier.Operation.ADDITION);
        setModifier(player, Attributes.MOVEMENT_SPEED, ABYSSAL_UNDERWATER_SPEED, "abyssal_crown_underwater_speed",
                abyssal && player.isInWaterOrBubble() ? 0.25D : 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setModifier(player, Attributes.ATTACK_DAMAGE, ABYSSAL_WATER_DAMAGE, "abyssal_crown_water_damage", waterBonus, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setModifier(player, Attributes.ARMOR, ABYSSAL_WATER_ARMOR, "abyssal_crown_water_armor", waterBonus, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setModifier(player, Attributes.MOVEMENT_SPEED, ABYSSAL_WATER_SPEED, "abyssal_crown_water_speed", waterBonus, AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static void setModifier(ServerPlayer player, Attribute attribute, UUID id, String name,
                                    double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier existing = instance.getModifier(id);
        if (Math.abs(amount) < 1.0E-9D) {
            if (existing != null) instance.removeModifier(id);
            return;
        }
        if (existing != null && existing.getAmount() == amount && existing.getOperation() == operation) return;
        if (existing != null) instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, TheCrownsMod.MOD_ID + ":" + name, amount, operation));
    }

    private static double waterBonus(ServerPlayer player) {
        if (player.isInWaterOrBubble()) return 0.25D;
        BlockPos origin = player.blockPosition();
        double closestSquared = Double.POSITIVE_INFINITY;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-20, -20, -20), origin.offset(20, 20, 20))) {
            FluidState fluid = player.level().getFluidState(pos);
            if (!fluid.is(FluidTags.WATER)) continue;
            double dx = pos.getX() + 0.5D - player.getX();
            double dy = pos.getY() + 0.5D - player.getY();
            double dz = pos.getZ() + 0.5D - player.getZ();
            double squared = dx * dx + dy * dy + dz * dz;
            if (squared <= 400.0D && squared < closestSquared) closestSquared = squared;
        }
        if (!Double.isFinite(closestSquared)) return 0.0D;
        return closestSquared >= 100.0D ? 0.10D : 0.15D;
    }

    public static void noteAttack(LivingEntity target, DamageSource source) {
        long now = target.level().getGameTime();
        Entity owner = source.getEntity();
        if (owner instanceof ServerPlayer attacker) {
            CompoundTag data = attacker.getPersistentData();
            if (hasShadowStealth(attacker)) {
                if (isShadowInvisible(attacker)) data.putLong(NBT_SHADOW_AMBUSH_TICK, now);
                data.putLong(NBT_SHADOW_LAST_COMBAT, now);
                data.remove(NBT_SHADOW_ACTIVE);
                if (!CrownLogic.isAuraActive(attacker)) {
                    attacker.setInvisible(false);
                    attacker.setSilent(false);
                }
            }
            if (hasAbyssalCore(attacker)) data.putLong(NBT_ABYSSAL_LAST_COMBAT, now);
        }
        if (target instanceof ServerPlayer victim && owner != null && owner != victim) {
            CompoundTag data = victim.getPersistentData();
            if (hasShadowStealth(victim)) {
                data.putLong(NBT_SHADOW_LAST_COMBAT, now);
                data.remove(NBT_SHADOW_ACTIVE);
                if (!CrownLogic.isAuraActive(victim)) {
                    victim.setInvisible(false);
                    victim.setSilent(false);
                }
            }
            if (hasAbyssalCore(victim)) data.putLong(NBT_ABYSSAL_LAST_COMBAT, now);
        }
    }

    /** Applies Ambush once at final-damage time, after all ordinary modifiers. */
    public static float applyAmbush(DamageSource source, float amount) {
        if (!(source.getEntity() instanceof ServerPlayer attacker)) return amount;
        CompoundTag data = attacker.getPersistentData();
        if (data.getLong(NBT_SHADOW_AMBUSH_TICK) != attacker.level().getGameTime()) return amount;
        data.remove(NBT_SHADOW_AMBUSH_TICK);
        return Math.min(NewCrownLogic.CROWN_DAMAGE_SAFETY_CAP, amount * 4.0F);
    }

    public static boolean shouldBlockTarget(Mob mob, ServerPlayer player) {
        return isShadowInvisible(player) || (hasAbyssalCore(player) && isAquatic(mob));
    }

    private static boolean isAquatic(Mob mob) {
        return mob instanceof WaterAnimal || mob.getMobType() == MobType.WATER
                || mob.getType() == EntityType.DROWNED || mob.isInWaterOrBubble();
    }

    private static void clearNearbyTargets(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(50.0D), mob -> mob instanceof Enemy)) {
            if (mob.getTarget() == player) mob.setTarget(null);
        }
    }
}
