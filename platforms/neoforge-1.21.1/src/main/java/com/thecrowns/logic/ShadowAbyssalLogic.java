package com.thecrowns.logic;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.registry.ModItems;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.material.FluidState;

/** Runtime abilities for the Shadow and Abyssal Tier I Crowns. */
public final class ShadowAbyssalLogic {
    private static final int SHADOW_STEALTH_DELAY = 160, ABYSSAL_COMBAT_DELAY = 60, ABYSSAL_DEEP_REGEN_DELAY = 200;
    private static final String SHADOW_LAST = "thecrowns_shadow_last_combat", SHADOW_ACTIVE = "thecrowns_shadow_active", SHADOW_AMBUSH = "thecrowns_shadow_ambush_tick";
    private static final String ABYSS_LAST = "thecrowns_abyssal_last_combat", ABYSS_WATER = "thecrowns_abyssal_water_ticks", ABYSS_BONUS = "thecrowns_abyssal_water_bonus", ABYSS_SCAN = "thecrowns_abyssal_water_scan";
    private static final ResourceLocation SHADOW_DAMAGE = id("shadow_crown_damage"), SHADOW_SPEED = id("shadow_crown_attack_speed"), ABYSS_HEALTH = id("abyssal_crown_health"), ABYSS_SWIM = id("abyssal_crown_underwater_speed"), ABYSS_DAMAGE = id("abyssal_crown_water_damage"), ABYSS_ARMOR = id("abyssal_crown_water_armor"), ABYSS_SPEED = id("abyssal_crown_water_speed");
    private ShadowAbyssalLogic() {}
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(TheCrownsMod.MOD_ID, path); }
    public static boolean isWearingShadow(LivingEntity entity) { return NewCrownLogic.isWearing(entity, ModItems.SHADOW_CROWN.get()); }
    public static boolean isWearingAbyssal(LivingEntity entity) { return NewCrownLogic.isWearing(entity, ModItems.ABYSSAL_CROWN.get()); }
    private static boolean hasShadowStealth(ServerPlayer player) { return isWearingShadow(player) || GlitchedFusionLogic.has(player, ModItems.SHADOW_CROWN.get()); }
    private static boolean hasAbyssalCore(ServerPlayer player) { return isWearingAbyssal(player) || GlitchedFusionLogic.has(player, ModItems.ABYSSAL_CROWN.get()); }
    public static boolean hasShadowHook(Entity entity) { return entity instanceof LivingEntity living && isWearingShadow(living); }
    public static boolean isShadowInvisible(ServerPlayer player) { return player != null && player.getPersistentData().getBoolean(SHADOW_ACTIVE) && hasShadowStealth(player); }

    public static void tickPlayer(ServerPlayer player) {
        long now = player.level().getGameTime(); CompoundTag data = player.getPersistentData();
        if (hasShadowStealth(player)) {
            if (!data.contains(SHADOW_LAST)) data.putLong(SHADOW_LAST, now);
            if (now - data.getLong(SHADOW_LAST) >= SHADOW_STEALTH_DELAY) { data.putBoolean(SHADOW_ACTIVE, true); player.setInvisible(true); player.setSilent(true); if (now % 10L == 0L) clearNearbyTargets(player); }
        } else {
            boolean active = data.getBoolean(SHADOW_ACTIVE); data.remove(SHADOW_ACTIVE); data.remove(SHADOW_LAST); data.remove(SHADOW_AMBUSH);
            if (active && !CrownLogic.isAuraActive(player)) { player.setInvisible(false); player.setSilent(false); }
        }
        boolean ownAbyssal = isWearingAbyssal(player);
        if (hasAbyssalCore(player)) {
            if (!data.contains(ABYSS_LAST)) data.putLong(ABYSS_LAST, now);
            if (ownAbyssal) player.removeEffect(MobEffects.DIG_SLOWDOWN);
            if (player.isInWaterOrBubble()) {
                player.setAirSupply(player.getMaxAirSupply()); data.putInt(ABYSS_WATER, data.getInt(ABYSS_WATER) + 1);
                if (now - data.getLong(ABYSS_LAST) >= ABYSSAL_COMBAT_DELAY && now % 20L == 0L && player.getHealth() < player.getMaxHealth()) player.heal(data.getInt(ABYSS_WATER) >= ABYSSAL_DEEP_REGEN_DELAY ? 5.0F : 1.0F);
            } else data.putInt(ABYSS_WATER, 0);
            if (ownAbyssal && now >= data.getLong(ABYSS_SCAN)) { data.putDouble(ABYSS_BONUS, waterBonus(player)); data.putLong(ABYSS_SCAN, now + 20L); }
            else if (!ownAbyssal) { data.remove(ABYSS_BONUS); data.remove(ABYSS_SCAN); }
        } else { data.remove(ABYSS_LAST); data.remove(ABYSS_WATER); data.remove(ABYSS_BONUS); data.remove(ABYSS_SCAN); }
        boolean shadow = isWearingShadow(player), abyss = isWearingAbyssal(player); double bonus = abyss ? data.getDouble(ABYSS_BONUS) : 0.0D;
        set(player, Attributes.ATTACK_DAMAGE, SHADOW_DAMAGE, shadow ? 7.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        set(player, Attributes.ATTACK_SPEED, SHADOW_SPEED, shadow ? .07D : 0.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        set(player, Attributes.MAX_HEALTH, ABYSS_HEALTH, abyss ? 10.0D : 0.0D, AttributeModifier.Operation.ADD_VALUE);
        set(player, Attributes.MOVEMENT_SPEED, ABYSS_SWIM, abyss && player.isInWaterOrBubble() ? .25D : 0.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        set(player, Attributes.ATTACK_DAMAGE, ABYSS_DAMAGE, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL); set(player, Attributes.ARMOR, ABYSS_ARMOR, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL); set(player, Attributes.MOVEMENT_SPEED, ABYSS_SPEED, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }
    private static void set(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation op) { AttributeInstance instance = player.getAttribute(attribute); if (instance == null) return; AttributeModifier old = instance.getModifier(id); if (amount == 0.0D) { if (old != null) instance.removeModifier(id); return; } if (old != null && old.amount() == amount && old.operation() == op) return; if (old != null) instance.removeModifier(id); instance.addTransientModifier(new AttributeModifier(id, amount, op)); }
    private static double waterBonus(ServerPlayer player) { if (player.isInWaterOrBubble()) return .25D; BlockPos o = player.blockPosition(); double closest = Double.POSITIVE_INFINITY; for (BlockPos pos : BlockPos.betweenClosed(o.offset(-20,-20,-20), o.offset(20,20,20))) { FluidState f = player.level().getFluidState(pos); if (!f.is(FluidTags.WATER)) continue; double x=pos.getX()+.5-player.getX(), y=pos.getY()+.5-player.getY(), z=pos.getZ()+.5-player.getZ(), d=x*x+y*y+z*z; if (d<=400D && d<closest) closest=d; } return !Double.isFinite(closest) ? 0D : closest >= 100D ? .10D : .15D; }
    public static void noteAttack(LivingEntity target, DamageSource source) { long now=target.level().getGameTime(); Entity owner=source.getEntity(); if (owner instanceof ServerPlayer attacker) { CompoundTag d=attacker.getPersistentData(); if (hasShadowStealth(attacker)) { if (isShadowInvisible(attacker)) d.putLong(SHADOW_AMBUSH,now); d.putLong(SHADOW_LAST,now); d.remove(SHADOW_ACTIVE); if (!CrownLogic.isAuraActive(attacker)) { attacker.setInvisible(false); attacker.setSilent(false); } } if (hasAbyssalCore(attacker)) d.putLong(ABYSS_LAST,now); } if (target instanceof ServerPlayer victim && owner != null && owner != victim) { CompoundTag d=victim.getPersistentData(); if (hasShadowStealth(victim)) { d.putLong(SHADOW_LAST,now); d.remove(SHADOW_ACTIVE); if (!CrownLogic.isAuraActive(victim)) { victim.setInvisible(false); victim.setSilent(false); } } if (hasAbyssalCore(victim)) d.putLong(ABYSS_LAST,now); } }
    public static float applyAmbush(DamageSource source, float amount) { if (!(source.getEntity() instanceof ServerPlayer player)) return amount; CompoundTag d=player.getPersistentData(); if (d.getLong(SHADOW_AMBUSH)!=player.level().getGameTime()) return amount; d.remove(SHADOW_AMBUSH); return Math.min(NewCrownLogic.CROWN_DAMAGE_SAFETY_CAP, amount*4F); }
    public static boolean shouldBlockTarget(Mob mob, ServerPlayer player) { return isShadowInvisible(player) || (hasAbyssalCore(player) && (mob instanceof WaterAnimal || mob.getType()==EntityType.DROWNED || mob.getType()==EntityType.GUARDIAN || mob.getType()==EntityType.ELDER_GUARDIAN || mob.isInWaterOrBubble())); }
    private static void clearNearbyTargets(ServerPlayer player) { if (!(player.level() instanceof ServerLevel level)) return; for (Mob mob: level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(50D), mob -> mob instanceof Enemy)) if (mob.getTarget()==player) mob.setTarget(null); }
}
