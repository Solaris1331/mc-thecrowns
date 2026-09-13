package com.thecrowns.logic;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.api.event.CrownAbilityTargetEvent;
import com.thecrowns.config.CrownServerConfig;
import com.thecrowns.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

/** Selective powers supplied by the Glitched Crown's single fusion slot. */
public final class GlitchedFusionLogic {
    private static final String NBT_ANGELIC_FLIGHT_ACTIVE = "thecrowns_fusion_angelic_flight_active";
    private static final String NBT_ANGELIC_BASE_FLY_SPEED = "thecrowns_fusion_angelic_base_fly_speed";

    private static final UUID BLOODY_HEALTH = uuid("fusion_bloody_health");
    private static final UUID ANGELIC_SPEED = uuid("fusion_angelic_speed");
    private static final UUID CURSED_HEALTH = uuid("fusion_cursed_health");
    private static final UUID CURSED_ARMOR = uuid("fusion_cursed_armor");
    private static final UUID CURSED_TOUGHNESS = uuid("fusion_cursed_toughness");
    private static final UUID CURSED_DAMAGE = uuid("fusion_cursed_damage");
    private static final UUID CURSED_LUCK = uuid("fusion_cursed_luck");
    private static final ThreadLocal<Boolean> BARRIER_REFLECTION_GUARD = ThreadLocal.withInitial(() -> false);

    private GlitchedFusionLogic() {}

    private static UUID uuid(String path) {
        return UUID.nameUUIDFromBytes((TheCrownsMod.MOD_ID + ":" + path).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean has(LivingEntity entity, Item crown) {
        return GlitchedCrownFusion.hasActiveFusion(entity, crown);
    }

    public static void onPlayerClone(Player original, Player replacement) {
        if (original == null || replacement == null) return;
        CompoundTag oldData = original.getPersistentData();
        if (oldData.getBoolean(NBT_ANGELIC_FLIGHT_ACTIVE)) {
            replacement.getAbilities().setFlyingSpeed(oldData.getFloat(NBT_ANGELIC_BASE_FLY_SPEED));
            replacement.getPersistentData().remove(NBT_ANGELIC_BASE_FLY_SPEED);
            replacement.getPersistentData().remove(NBT_ANGELIC_FLIGHT_ACTIVE);
            if (replacement instanceof ServerPlayer serverPlayer) serverPlayer.onUpdateAbilities();
        }
    }

    public static void tickPlayer(ServerPlayer player) {
        ResourceLocation fusion = GlitchedCrownFusion.getActiveFusion(player);
        boolean bloody = GlitchedCrownFusion.matches(fusion, ModItems.BLOODY_CROWN.get());
        boolean angelic = GlitchedCrownFusion.matches(fusion, ModItems.ANGELIC_CROWN.get());
        boolean cursed = GlitchedCrownFusion.matches(fusion, ModItems.CURSED_CROWN.get());

        setModifier(player, Attributes.MAX_HEALTH, BLOODY_HEALTH, "fusion_bloody_health",
                bloody ? 40.0D : 0.0D, AttributeModifier.Operation.ADDITION);

        setModifier(player, Attributes.MOVEMENT_SPEED, ANGELIC_SPEED, "fusion_angelic_speed",
                angelic ? 0.50D : 0.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        tickAngelicFlightSpeed(player, angelic);

        boolean cursedPenalty = cursed && AdvancedCrownLogic.isOwnCursedPenaltyActive(player);
        applyPostGlitchedPenalty(player, Attributes.MAX_HEALTH, CURSED_HEALTH, "fusion_cursed_health",
                CrownServerConfig.BONUS_MAX_HEALTH.get(), cursedPenalty);
        applyPostGlitchedPenalty(player, Attributes.ARMOR, CURSED_ARMOR, "fusion_cursed_armor",
                CrownServerConfig.BONUS_ARMOR.get(), cursedPenalty);
        applyPostGlitchedPenalty(player, Attributes.ARMOR_TOUGHNESS, CURSED_TOUGHNESS, "fusion_cursed_toughness",
                CrownServerConfig.BONUS_TOUGHNESS.get(), cursedPenalty);
        applyPostFinalDamageCorrection(player, cursedPenalty);
        setModifier(player, Attributes.LUCK, CURSED_LUCK, "fusion_cursed_luck",
                cursed ? CrownServerConfig.CURSED_LUCK_BONUS.get() : 0.0D, AttributeModifier.Operation.ADDITION);

        if (GlitchedCrownFusion.matches(fusion, ModItems.DIVINE_CROWN.get())
                && player.tickCount % (5 * 20) == 0) {
            healNearbyAllies(player, player.getMaxHealth() * 0.05F, false);
        }
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static void tickAngelicFlightSpeed(ServerPlayer player, boolean activeFusion) {
        CompoundTag data = player.getPersistentData();
        boolean active = data.getBoolean(NBT_ANGELIC_FLIGHT_ACTIVE);
        if (activeFusion && !active) {
            float base = player.getAbilities().getFlyingSpeed();
            data.putFloat(NBT_ANGELIC_BASE_FLY_SPEED, base);
            data.putBoolean(NBT_ANGELIC_FLIGHT_ACTIVE, true);
            player.getAbilities().setFlyingSpeed(base * 1.50F);
            player.onUpdateAbilities();
        } else if (activeFusion) {
            float expected = data.getFloat(NBT_ANGELIC_BASE_FLY_SPEED) * 1.50F;
            if (Math.abs(player.getAbilities().getFlyingSpeed() - expected) > 0.000001F) {
                player.getAbilities().setFlyingSpeed(expected);
                player.onUpdateAbilities();
            }
        } else if (active) {
            player.getAbilities().setFlyingSpeed(data.getFloat(NBT_ANGELIC_BASE_FLY_SPEED));
            data.remove(NBT_ANGELIC_BASE_FLY_SPEED);
            data.remove(NBT_ANGELIC_FLIGHT_ACTIVE);
            player.onUpdateAbilities();
        }
    }

    /**
     * Applies the Curse to the pre-Glitched value while preserving the configured
     * Glitched additive bonus at full value after the penalty.
     */
    private static void applyPostGlitchedPenalty(ServerPlayer player, Attribute attribute, UUID id, String name,
                                                  double glitchedAddition, boolean active) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier existing = instance.getModifier(id);
        if (!active) {
            if (existing != null) instance.removeModifier(id);
            return;
        }

        double factor = 1.0D;
        for (AttributeModifier modifier : instance.getModifiers(AttributeModifier.Operation.MULTIPLY_BASE)) {
            factor += modifier.getAmount();
        }
        for (AttributeModifier modifier : instance.getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
            factor *= 1.0D + modifier.getAmount();
        }
        if (!Double.isFinite(factor) || Math.abs(factor) < 1.0E-9D) return;

        double fullValue = instance.getValue()
                - (existing == null ? 0.0D : existing.getAmount() * factor);
        double glitchedContribution = glitchedAddition * factor;
        double preGlitchedValue = fullValue - glitchedContribution;
        double fraction = CrownServerConfig.CURSED_PENALTY_FRACTION.get();
        double targetValue = preGlitchedValue * (1.0D - fraction) + glitchedContribution;
        double addition = (targetValue - fullValue) / factor;
        if (Double.isFinite(addition) && Math.abs(addition) > 1.0E-9D) {
            if (existing != null && Double.compare(existing.getAmount(), addition) == 0) return;
            if (existing != null) instance.removeModifier(id);
            instance.addTransientModifier(new AttributeModifier(id, TheCrownsMod.MOD_ID + ":" + name,
                    addition, AttributeModifier.Operation.ADDITION));
        } else if (existing != null) {
            instance.removeModifier(id);
        }
    }

    /** Compensates only the Glitched attack bonus for the later outgoing-final-damage Curse multiplier. */
    private static void applyPostFinalDamageCorrection(ServerPlayer player, boolean active) {
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance == null) return;
        AttributeModifier existing = instance.getModifier(CURSED_DAMAGE);
        if (!active) {
            if (existing != null) instance.removeModifier(CURSED_DAMAGE);
            return;
        }
        double fraction = CrownServerConfig.CURSED_PENALTY_FRACTION.get();
        double remaining = 1.0D - fraction;
        double addition = remaining <= 1.0E-9D ? 0.0D
                : CrownServerConfig.BONUS_ATTACK_DAMAGE.get() * fraction / remaining;
        if (existing != null && Double.compare(existing.getAmount(), addition) == 0) return;
        if (existing != null) instance.removeModifier(CURSED_DAMAGE);
        if (Double.isFinite(addition) && Math.abs(addition) > 1.0E-9D) {
            instance.addTransientModifier(new AttributeModifier(CURSED_DAMAGE,
                    TheCrownsMod.MOD_ID + ":fusion_cursed_damage", addition,
                    AttributeModifier.Operation.ADDITION));
        }
    }

    private static void setModifier(ServerPlayer player, Attribute attribute, UUID id, String name,
                                    double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier existing = instance.getModifier(id);
        if (amount == 0.0D) {
            if (existing != null) instance.removeModifier(id);
            return;
        }
        if (existing != null && Double.compare(existing.getAmount(), amount) == 0
                && existing.getOperation() == operation) return;
        if (existing != null) instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, TheCrownsMod.MOD_ID + ":" + name,
                amount, operation));
    }

    public static void onBarrierConsumed(ServerPlayer player) {
        if (BARRIER_REFLECTION_GUARD.get() || !has(player, ModItems.BURNING_CROWN.get())
                || !(player.level() instanceof ServerLevel level)) return;
        double radius = 2.0D;
        float damage = (float) (4.0D + player.getAttributeValue(Attributes.ARMOR) * 0.5D);
        BARRIER_REFLECTION_GUARD.set(true);
        try {
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(radius), living -> living != player && living.isAlive()
                            && living.distanceToSqr(player) <= radius * radius && isHostileTo(player, living)
                            && !CrownTargeting.isProtectedTarget(player, living))) {
                if (!CrownLogic.canAffectCrownTarget(player, target, CrownAbilityTargetEvent.Ability.ABSOLUTE_DAMAGE)) continue;
                target.invulnerableTime = 0;
                target.hurt(player.damageSources().thorns(player), damage);
            }
        } finally {
            BARRIER_REFLECTION_GUARD.set(false);
        }
    }

    public static boolean isHostileTo(ServerPlayer player, LivingEntity target) {
        if (target instanceof Enemy) return true;
        if (target instanceof NeutralMob neutral && neutral.isAngryAt(player)) return true;
        if (target instanceof Mob mob && mob.getTarget() == player) return true;
        return target instanceof Player && !CrownTargeting.isProtectedTarget(player, target);
    }

    public static boolean activateJAbility(ServerPlayer player) {
        if (has(player, ModItems.IRONFORGED_CROWN.get())) {
            NewCrownLogic.summonIronGolem(player);
            return true;
        }
        if (has(player, ModItems.CURSED_CROWN.get())) {
            AdvancedCrownLogic.activateLiberation(player);
            return true;
        }
        return false;
    }

    public static int glitchedReviveCooldownTicks(ServerPlayer player) {
        int normal = CrownLogic.glitchedReviveCooldownTicks();
        return has(player, ModItems.ANGELIC_CROWN.get()) ? normal / 2 : normal;
    }

    public static int glitchedLaserCooldownTicks(ServerPlayer player) {
        int normal = CrownLogic.glitchedLaserCooldownTicks();
        return has(player, ModItems.TEMPORAL_CROWN.get()) ? Math.max(0, normal - 30 * 20) : normal;
    }

    public static boolean doublesBarrierRecharge(ServerPlayer player) {
        return has(player, ModItems.TEMPORAL_CROWN.get()) && AdvancedCrownLogic.isTemporalOutOfCombat(player);
    }

    public static void onGlitchedRevive(ServerPlayer player) {
        if (has(player, ModItems.DIVINE_CROWN.get())) {
            healNearbyAllies(player, Float.POSITIVE_INFINITY, true);
        }
        if (has(player, ModItems.CURSED_CROWN.get())) {
            AdvancedCrownLogic.resetCursedLiberationCooldown(player);
        }
    }

    private static void healNearbyAllies(ServerPlayer player, float amount, boolean cleanse) {
        if (!(player.level() instanceof ServerLevel level)) return;
        double radius = CrownServerConfig.DIVINE_SANCTIFY_RADIUS.get();
        double radiusSqr = radius * radius;
        for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                living -> living != player && living.distanceToSqr(player) <= radiusSqr
                        && AdvancedCrownLogic.isSanctifyAlly(player, living))) {
            if (cleanse) {
                for (MobEffectInstance effect : new ArrayList<>(ally.getActiveEffects())) {
                    if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                        ally.removeEffect(effect.getEffect());
                    }
                }
                ally.setHealth(ally.getMaxHealth());
            } else if (amount > 0.0F) {
                ally.setHealth(Math.min(ally.getMaxHealth(), ally.getHealth() + amount));
            }
        }
    }
}
