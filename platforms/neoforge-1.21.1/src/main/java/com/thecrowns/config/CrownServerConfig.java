package com.thecrowns.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-authoritative pack-developer configuration.
 *
 * The Unleashed Unleashed Crown intentionally ignores every option in this
 * class; it is the immutable full-power creative/admin reference item.
 */
public final class CrownServerConfig {
    /**
     * NORMAL: ordinary damage pipeline.
     * BYPASS_TAGS: vanilla/Forge bypass tags are supplied, but downstream mod events still run.
     * RAW_HEALTH: 1.1.12 behavior; offensive Hurt modifiers resolve, then body HP is changed directly.
     */
    public enum UnleashedDamageMode { NORMAL, BYPASS_TAGS, RAW_HEALTH }
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING;
    public static final ModConfigSpec.IntValue CROWN_SLOT_COUNT;

    public static final ModConfigSpec.BooleanValue PROTECT_FTB_TEAMS;
    public static final ModConfigSpec.BooleanValue PROTECT_SCOREBOARD_TEAMS;
    public static final ModConfigSpec.BooleanValue PROTECT_TAMED;
    public static final ModConfigSpec.BooleanValue PROTECT_OWNED;

    public static final ModConfigSpec.DoubleValue TEMPORAL_MOVEMENT_SPEED_BONUS;
    public static final ModConfigSpec.DoubleValue TEMPORAL_ATTACK_SPEED_BONUS;
    public static final ModConfigSpec.DoubleValue TEMPORAL_FLIGHT_SPEED_BONUS;
    public static final ModConfigSpec.DoubleValue TEMPORAL_CONSUMABLE_SPEED_MULTIPLIER;
    public static final ModConfigSpec.IntValue TEMPORAL_OUT_OF_COMBAT_SECONDS;
    public static final ModConfigSpec.IntValue TEMPORAL_REWIND_COOLDOWN_SECONDS;
    public static final ModConfigSpec.IntValue TEMPORAL_REWIND_CONCENTRATION_SECONDS;
    public static final ModConfigSpec.IntValue TEMPORAL_WARP_COOLDOWN_SECONDS;
    public static final ModConfigSpec.IntValue TEMPORAL_WARP_DURATION_SECONDS;
    public static final ModConfigSpec.DoubleValue TEMPORAL_WARP_RADIUS;
    public static final ModConfigSpec.DoubleValue TEMPORAL_WARP_STOP_RADIUS;
    public static final ModConfigSpec.DoubleValue TEMPORAL_WARP_SLOW_FRACTION;

    public static final ModConfigSpec.DoubleValue FROST_ATTACK_DAMAGE;
    public static final ModConfigSpec.DoubleValue FROST_ARMOR;
    public static final ModConfigSpec.DoubleValue FROST_TOUGHNESS;
    public static final ModConfigSpec.DoubleValue FROST_AFFINITY_RADIUS;
    public static final ModConfigSpec.DoubleValue FROST_COLD_BIOME_TEMPERATURE;
    public static final ModConfigSpec.IntValue FROST_STACKS_TO_FREEZE;
    public static final ModConfigSpec.DoubleValue FROST_SLOW_PER_STACK;
    public static final ModConfigSpec.DoubleValue FROST_STACK_INTERNAL_COOLDOWN_SECONDS;
    public static final ModConfigSpec.IntValue FROST_DECAY_SECONDS;
    public static final ModConfigSpec.IntValue FROST_FREEZE_SECONDS;
    public static final ModConfigSpec.IntValue FROST_BOSS_PLAYER_FREEZE_SECONDS;
    public static final ModConfigSpec.IntValue FROST_REFREEZE_COOLDOWN_SECONDS;
    public static final ModConfigSpec.IntValue FROST_BOSS_PLAYER_REFREEZE_COOLDOWN_SECONDS;
    public static final ModConfigSpec.DoubleValue FROST_BONUS_DAMAGE;

    public static final ModConfigSpec.DoubleValue DIVINE_FLAT_MAX_HEALTH;
    public static final ModConfigSpec.DoubleValue DIVINE_PERCENT_MAX_HEALTH;
    public static final ModConfigSpec.DoubleValue DIVINE_ARMOR;
    public static final ModConfigSpec.DoubleValue DIVINE_TOUGHNESS;
    public static final ModConfigSpec.DoubleValue DIVINE_HEALING_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue DIVINE_NEGATIVE_EFFECT_BASE_HEAL;
    public static final ModConfigSpec.DoubleValue DIVINE_NEGATIVE_EFFECT_MAX_HEALTH_FRACTION;
    public static final ModConfigSpec.DoubleValue DIVINE_NEGATIVE_EFFECT_COOLDOWN_SECONDS;
    public static final ModConfigSpec.DoubleValue DIVINE_POSITIVE_EFFECT_DURATION_FRACTION;
    public static final ModConfigSpec.IntValue DIVINE_SANCTIFY_COOLDOWN_SECONDS;
    public static final ModConfigSpec.IntValue DIVINE_SANCTIFY_SOLO_REDUCTION_SECONDS;
    public static final ModConfigSpec.DoubleValue DIVINE_SANCTIFY_RADIUS;

    public static final ModConfigSpec.DoubleValue CURSED_PENALTY_FRACTION;
    public static final ModConfigSpec.IntValue CURSED_LOOTING_BONUS;
    public static final ModConfigSpec.DoubleValue CURSED_LUCK_BONUS;
    public static final ModConfigSpec.DoubleValue CURSED_EXPERIENCE_BONUS_FRACTION;
    public static final ModConfigSpec.IntValue CURSED_LIBERATION_DURATION_SECONDS;
    public static final ModConfigSpec.IntValue CURSED_LIBERATION_COOLDOWN_SECONDS;
    public static final ModConfigSpec.DoubleValue CURSED_LIBERATION_RADIUS;
    public static final ModConfigSpec.DoubleValue CURSED_LIBERATION_HEAL_MAX_HEALTH_MULTIPLIER;

    public static final ModConfigSpec.BooleanValue GLITCHED_RECIPE;
    public static final ModConfigSpec.BooleanValue GLITCHED_ADVANCEMENT_GATE;
    public static final ModConfigSpec.IntValue GLITCHED_ADVANCEMENT_POINTS;
    public static final ModConfigSpec.ConfigValue<String> GLITCHED_CORE_ADVANCEMENTS;
    public static final ModConfigSpec.ConfigValue<String> GLITCHED_SUBSTITUTE_ADVANCEMENTS;
    public static final ModConfigSpec.BooleanValue GLITCHED_FLIGHT;
    public static final ModConfigSpec.BooleanValue GLITCHED_SHIELD;
    public static final ModConfigSpec.BooleanValue GLITCHED_REVIVE;
    public static final ModConfigSpec.BooleanValue GLITCHED_REMOVAL_RAY;
    public static final ModConfigSpec.BooleanValue GLITCHED_EXECUTION_AURA;
    public static final ModConfigSpec.BooleanValue GLITCHED_NULLIFICATION_AURA;
    public static final ModConfigSpec.BooleanValue GLITCHED_MOVEMENT_IMMUNITY;
    public static final ModConfigSpec.BooleanValue GLITCHED_STATUS_IMMUNITY;
    public static final ModConfigSpec.BooleanValue GLITCHED_NIGHT_VISION;
    public static final ModConfigSpec.BooleanValue GLITCHED_BENEFICIAL_EFFECT_PROTECTION;
    public static final ModConfigSpec.IntValue GLITCHED_SHIELD_STACKS;
    public static final ModConfigSpec.IntValue GLITCHED_SHIELD_RECHARGE_SECONDS;
    public static final ModConfigSpec.DoubleValue GLITCHED_SHIELD_DAMAGE_MULTIPLIER;
    public static final ModConfigSpec.IntValue GLITCHED_REVIVE_COOLDOWN_SECONDS;
    public static final ModConfigSpec.IntValue GLITCHED_REVIVE_INVULN_SECONDS;
    public static final ModConfigSpec.IntValue GLITCHED_REMOVAL_RAY_COOLDOWN_SECONDS;
    public static final ModConfigSpec.DoubleValue GLITCHED_REMOVAL_RAY_RANGE;
    public static final ModConfigSpec.DoubleValue GLITCHED_REMOVAL_RAY_RADIUS;
    public static final ModConfigSpec.DoubleValue GLITCHED_RAY_NORMAL_FRACTION;
    public static final ModConfigSpec.DoubleValue GLITCHED_RAY_PLAYER_FRACTION;
    public static final ModConfigSpec.DoubleValue GLITCHED_RAY_BOSS_FRACTION;
    public static final ModConfigSpec.DoubleValue GLITCHED_EXECUTION_THRESHOLD;
    public static final ModConfigSpec.DoubleValue GLITCHED_NULLIFICATION_RADIUS;

    public static final ModConfigSpec.BooleanValue UNLEASHED_RECIPE;
    public static final ModConfigSpec.BooleanValue UNLEASHED_REQUIRE_GLITCHED_ADVANCEMENT;
    public static final ModConfigSpec.BooleanValue UNLEASHED_FLIGHT;
    public static final ModConfigSpec.BooleanValue UNLEASHED_INVULNERABILITY;
    public static final ModConfigSpec.BooleanValue UNLEASHED_INFINITE_REVIVE;
    public static final ModConfigSpec.BooleanValue UNLEASHED_REMOVAL_RAY;
    public static final ModConfigSpec.BooleanValue UNLEASHED_FATE;
    public static final ModConfigSpec.BooleanValue UNLEASHED_OBLIVION_VEIL;
    public static final ModConfigSpec.BooleanValue UNLEASHED_ANNIHILATION;
    public static final ModConfigSpec.BooleanValue UNLEASHED_DEFENSE_BYPASS;
    public static final ModConfigSpec.EnumValue<UnleashedDamageMode> UNLEASHED_DAMAGE_MODE;
    public static final ModConfigSpec.BooleanValue UNLEASHED_UTILITY_EFFECTS;
    public static final ModConfigSpec.BooleanValue UNLEASHED_MOVEMENT_IMMUNITY;
    public static final ModConfigSpec.BooleanValue UNLEASHED_STATUS_IMMUNITY;
    public static final ModConfigSpec.BooleanValue UNLEASHED_BENEFICIAL_EFFECT_PROTECTION;
    public static final ModConfigSpec.BooleanValue UNLEASHED_NULLIFICATION_AURA;
    public static final ModConfigSpec.BooleanValue UNLEASHED_INTEGRITY_PROTECTION;
    public static final ModConfigSpec.DoubleValue UNLEASHED_NULLIFICATION_RADIUS;
    public static final ModConfigSpec.DoubleValue UNLEASHED_REMOVAL_RAY_RANGE;
    public static final ModConfigSpec.DoubleValue UNLEASHED_REMOVAL_RAY_RADIUS;
    public static final ModConfigSpec.DoubleValue UNLEASHED_REMOVAL_RAY_MAX_HEALTH_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue UNLEASHED_REMOVAL_RAY_ERASE_REMAINDER;
    public static final ModConfigSpec.DoubleValue UNLEASHED_ANNIHILATION_RADIUS;

    public static final ModConfigSpec.DoubleValue BONUS_MAX_HEALTH;
    public static final ModConfigSpec.DoubleValue BONUS_ARMOR;
    public static final ModConfigSpec.DoubleValue BONUS_TOUGHNESS;
    public static final ModConfigSpec.DoubleValue BONUS_LUCK;
    public static final ModConfigSpec.IntValue BONUS_LOOTING;
    public static final ModConfigSpec.DoubleValue BONUS_ATTACK_DAMAGE;
    public static final ModConfigSpec.DoubleValue BONUS_KNOCKBACK_RESISTANCE;
    public static final ModConfigSpec.DoubleValue BONUS_INTERACTION_REACH;
    public static final ModConfigSpec.DoubleValue UNLEASHED_BONUS_MAX_HEALTH;
    public static final ModConfigSpec.DoubleValue UNLEASHED_BONUS_ARMOR;
    public static final ModConfigSpec.DoubleValue UNLEASHED_BONUS_TOUGHNESS;
    public static final ModConfigSpec.DoubleValue UNLEASHED_BONUS_LUCK;
    public static final ModConfigSpec.IntValue UNLEASHED_BONUS_LOOTING;
    public static final ModConfigSpec.DoubleValue UNLEASHED_BONUS_ATTACK_DAMAGE;
    public static final ModConfigSpec.DoubleValue UNLEASHED_BONUS_KNOCKBACK_RESISTANCE;
    public static final ModConfigSpec.DoubleValue UNLEASHED_BONUS_INTERACTION_REACH;

    /** Legacy config entries retained only so existing config files remain readable; no crit bonus is applied. */
    public static final ModConfigSpec.DoubleValue FATE_HALF_CRIT_CHANCE;
    public static final ModConfigSpec.DoubleValue FATE_HALF_CRIT_DAMAGE;
    public static final ModConfigSpec.DoubleValue FATE_FULL_CRIT_CHANCE;
    public static final ModConfigSpec.DoubleValue FATE_FULL_CRIT_DAMAGE;
    public static final ModConfigSpec.DoubleValue FATE_HALF_DAMAGE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue FATE_FULL_DAMAGE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue FATE_ARMOR_PIERCE;
    public static final ModConfigSpec.DoubleValue FATE_PROTECTION_SHRED;
    public static final ModConfigSpec.DoubleValue FATE_HALF_ATTACK_SPEED;
    public static final ModConfigSpec.DoubleValue FATE_HALF_ATTACK_DAMAGE;
    public static final ModConfigSpec.DoubleValue FATE_FULL_ATTACK_SPEED;
    public static final ModConfigSpec.DoubleValue FATE_FULL_ATTACK_DAMAGE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("general");
        DEBUG_LOGGING = b.comment("Log blocked movement/target decisions for pack debugging.")
                .define("debugLogging", false);
        b.pop();

        b.comment("Curios Crown slot settings.").push("curios");
        CROWN_SLOT_COUNT = b.comment("Number of dedicated Crown slots available to each player.")
                .defineInRange("crownSlotCount", 1, 1, 16);
        b.pop();

        b.push("targeting");
        PROTECT_FTB_TEAMS = b.define("protectSameFTBTeam", true);
        PROTECT_SCOREBOARD_TEAMS = b.define("protectSameScoreboardTeam", true);
        PROTECT_TAMED = b.define("protectTamedEntities", true);
        PROTECT_OWNED = b.define("protectOwnedEntities", true);
        b.pop();

        b.comment("Time Crown tuning. Fractions use 0.20 = 20%.").push("temporalCrown");
        TEMPORAL_MOVEMENT_SPEED_BONUS = b.defineInRange("movementSpeedBonus", 0.20D, -0.99D, 1000.0D);
        TEMPORAL_ATTACK_SPEED_BONUS = b.defineInRange("attackSpeedBonus", 0.20D, -0.99D, 1000.0D);
        TEMPORAL_FLIGHT_SPEED_BONUS = b.defineInRange("flightSpeedBonus", 0.20D, -0.99D, 1000.0D);
        TEMPORAL_CONSUMABLE_SPEED_MULTIPLIER = b.defineInRange("consumableSpeedMultiplier", 1.50D, 0.01D, 1000.0D);
        TEMPORAL_OUT_OF_COMBAT_SECONDS = b.defineInRange("outOfCombatSeconds", 10, 0, 86400);
        TEMPORAL_REWIND_COOLDOWN_SECONDS = b.defineInRange("rewindCooldownSeconds", 300, 0, 864000);
        TEMPORAL_REWIND_CONCENTRATION_SECONDS = b.defineInRange("rewindConcentrationSeconds", 3, 0, 86400);
        TEMPORAL_WARP_COOLDOWN_SECONDS = b.defineInRange("timeWarpCooldownSeconds", 180, 0, 864000);
        TEMPORAL_WARP_DURATION_SECONDS = b.defineInRange("timeWarpDurationSeconds", 15, 1, 86400);
        TEMPORAL_WARP_RADIUS = b.defineInRange("timeWarpRadius", 20.0D, 0.0D, 4096.0D);
        TEMPORAL_WARP_STOP_RADIUS = b.defineInRange("timeWarpStopRadius", 10.0D, 0.0D, 4096.0D);
        TEMPORAL_WARP_SLOW_FRACTION = b.defineInRange("timeWarpSlowFraction", 0.50D, 0.0D, 1.0D);
        b.pop();

        b.comment("Frost Crown tuning. Fractions use 0.10 = 10%.").push("frostCrown");
        FROST_ATTACK_DAMAGE = b.defineInRange("attackDamage", 5.0D, -1000000.0D, 1000000.0D);
        FROST_ARMOR = b.defineInRange("armor", 5.0D, -1000000.0D, 1000000.0D);
        FROST_TOUGHNESS = b.defineInRange("armorToughness", 5.0D, -1000000.0D, 1000000.0D);
        FROST_AFFINITY_RADIUS = b.defineInRange("coldCreatureAffinityRadius", 48.0D, 0.0D, 4096.0D);
        FROST_COLD_BIOME_TEMPERATURE = b.defineInRange("coldBiomeTemperatureThreshold", 0.15D, -10.0D, 10.0D);
        FROST_STACKS_TO_FREEZE = b.defineInRange("stacksToFreeze", 5, 1, 1000);
        FROST_SLOW_PER_STACK = b.defineInRange("slowFractionPerStack", 0.10D, 0.0D, 1.0D);
        FROST_STACK_INTERNAL_COOLDOWN_SECONDS = b.defineInRange("stackInternalCooldownSeconds", 0.50D, 0.0D, 86400.0D);
        FROST_DECAY_SECONDS = b.defineInRange("stackDecaySeconds", 5, 1, 86400);
        FROST_FREEZE_SECONDS = b.defineInRange("freezeDurationSeconds", 3, 1, 86400);
        FROST_BOSS_PLAYER_FREEZE_SECONDS = b.defineInRange("bossPlayerFreezeDurationSeconds", 2, 1, 86400);
        FROST_REFREEZE_COOLDOWN_SECONDS = b.defineInRange("refreezeCooldownSeconds", 10, 0, 86400);
        FROST_BOSS_PLAYER_REFREEZE_COOLDOWN_SECONDS = b.defineInRange("bossPlayerRefreezeCooldownSeconds", 20, 0, 86400);
        FROST_BONUS_DAMAGE = b.defineInRange("bonusColdDamage", 3.0D, 0.0D, 1000000.0D);
        b.pop();

        b.comment("Divine Crown tuning. Fractions use 0.30 = 30%.").push("divineCrown");
        DIVINE_FLAT_MAX_HEALTH = b.defineInRange("flatMaxHealth", 25.0D, -1000000.0D, 1000000.0D);
        DIVINE_PERCENT_MAX_HEALTH = b.defineInRange("percentMaxHealth", 0.25D, -0.99D, 1000.0D);
        DIVINE_ARMOR = b.defineInRange("armor", 5.0D, -1000000.0D, 1000000.0D);
        DIVINE_TOUGHNESS = b.defineInRange("armorToughness", 0.0D, -1000000.0D, 1000000.0D);
        DIVINE_HEALING_MULTIPLIER = b.defineInRange("healingReceivedMultiplier", 1.35D, 0.0D, 1000.0D);
        DIVINE_NEGATIVE_EFFECT_BASE_HEAL = b.defineInRange("negativeEffectBaseHeal", 3.0D, 0.0D, 1000000.0D);
        DIVINE_NEGATIVE_EFFECT_MAX_HEALTH_FRACTION = b.defineInRange("negativeEffectMaxHealthHealFraction", 0.03D, 0.0D, 1000.0D);
        DIVINE_NEGATIVE_EFFECT_COOLDOWN_SECONDS = b.defineInRange("negativeEffectCooldownSeconds", 3.0D, 0.0D, 86400.0D);
        DIVINE_POSITIVE_EFFECT_DURATION_FRACTION = b.defineInRange("positiveEffectDurationFraction", 0.50D, 0.0D, 1000.0D);
        DIVINE_SANCTIFY_COOLDOWN_SECONDS = b.defineInRange("sanctifyCooldownSeconds", 180, 0, 864000);
        DIVINE_SANCTIFY_SOLO_REDUCTION_SECONDS = b.defineInRange("sanctifySoloCooldownReductionSeconds", 90, 0, 864000);
        DIVINE_SANCTIFY_RADIUS = b.defineInRange("sanctifyRadius", 7.0D, 0.0D, 4096.0D);
        b.pop();

        b.comment("Cursed Crown tuning. penaltyFraction 0.50 means a 50% penalty.").push("cursedCrown");
        CURSED_PENALTY_FRACTION = b.defineInRange("penaltyFraction", 0.50D, 0.0D, 1.0D);
        CURSED_LOOTING_BONUS = b.defineInRange("lootingBonus", 7, -1000000, 1000000);
        CURSED_LUCK_BONUS = b.defineInRange("luckBonus", 7.0D, -1000000.0D, 1000000.0D);
        CURSED_EXPERIENCE_BONUS_FRACTION = b.defineInRange("experienceBonusFraction", 3.0D, 0.0D, 1000000.0D);
        CURSED_LIBERATION_DURATION_SECONDS = b.defineInRange("liberationDurationSeconds", 60, 1, 86400);
        CURSED_LIBERATION_COOLDOWN_SECONDS = b.defineInRange("liberationCooldownSeconds", 600, 0, 864000);
        CURSED_LIBERATION_RADIUS = b.defineInRange("liberationRadius", 30.0D, 0.0D, 4096.0D);
        CURSED_LIBERATION_HEAL_MAX_HEALTH_MULTIPLIER = b.defineInRange("liberationHealMaxHealthMultiplier", 1.0D, 0.0D, 1000.0D);
        b.pop();

        b.push("sharedStats");
        BONUS_MAX_HEALTH = b.defineInRange("maxHealth", 80.0D, -1000000.0D, 1000000.0D);
        BONUS_ARMOR = b.defineInRange("armor", 40.0D, -1000000.0D, 1000000.0D);
        BONUS_TOUGHNESS = b.defineInRange("armorToughness", 40.0D, -1000000.0D, 1000000.0D);
        BONUS_LUCK = b.defineInRange("luck", 7.0D, -1000000.0D, 1000000.0D);
        BONUS_LOOTING = b.defineInRange("lootingLevel", 7, -1000000, 1000000);
        BONUS_ATTACK_DAMAGE = b.defineInRange("attackDamage", 40.0D, -1000000.0D, 1000000.0D);
        BONUS_KNOCKBACK_RESISTANCE = b.defineInRange("knockbackResistance", 1.0D, 0.0D, 1000.0D);
        BONUS_INTERACTION_REACH = b.defineInRange("interactionReach", 3.5D, -1000.0D, 1000.0D);
        b.pop();

        b.comment("Unleashed Crown base stats. Kept separate from the Glitched Crown shared stats.").push("unleashedStats");
        UNLEASHED_BONUS_MAX_HEALTH = b.defineInRange("maxHealth", 160.0D, -1000000.0D, 1000000.0D);
        UNLEASHED_BONUS_ARMOR = b.defineInRange("armor", 80.0D, -1000000.0D, 1000000.0D);
        UNLEASHED_BONUS_TOUGHNESS = b.defineInRange("armorToughness", 80.0D, -1000000.0D, 1000000.0D);
        UNLEASHED_BONUS_LUCK = b.defineInRange("luck", 14.0D, -1000000.0D, 1000000.0D);
        UNLEASHED_BONUS_LOOTING = b.defineInRange("lootingLevel", 14, -1000000, 1000000);
        UNLEASHED_BONUS_ATTACK_DAMAGE = b.defineInRange("attackDamage", 80.0D, -1000000.0D, 1000000.0D);
        UNLEASHED_BONUS_KNOCKBACK_RESISTANCE = b.defineInRange("knockbackResistance", 2.0D, 0.0D, 1000.0D);
        UNLEASHED_BONUS_INTERACTION_REACH = b.defineInRange("interactionReach", 7.0D, -1000.0D, 1000.0D);
        b.pop();

        b.push("glitched");
        GLITCHED_RECIPE = b.define("enableRecipe", true);
        GLITCHED_ADVANCEMENT_GATE = b.define("enableAdvancementGate", true);
        GLITCHED_ADVANCEMENT_POINTS = b.defineInRange("requiredAdvancementPoints", 4, 0, 128);
        GLITCHED_CORE_ADVANCEMENTS = b.comment("Comma-separated advancement ids. Each completed entry gives one point.")
                .define("coreAdvancements", "minecraft:end/kill_dragon,minecraft:nether/summon_wither,minecraft:adventure/kill_mob_near_sculk_catalyst,minecraft:nether/netherite_armor");
        GLITCHED_SUBSTITUTE_ADVANCEMENTS = b.comment("Comma-separated substitute advancement ids. Most entries require two completions per one advancement point; all_potions and all_effects each count as one full point.")
                .define("substituteAdvancements", "minecraft:nether/fast_travel,minecraft:nether/uneasy_alliance,minecraft:nether/all_potions,minecraft:nether/all_effects,minecraft:nether/explore_nether,minecraft:end/levitate,minecraft:adventure/kill_all_mobs,minecraft:adventure/sniper_duel,minecraft:adventure/arbalistic,minecraft:adventure/bullseye,minecraft:nether/create_full_beacon,minecraft:adventure/hero_of_the_village,minecraft:husbandry/obtain_netherite_hoe,minecraft:adventure/very_very_frightening,minecraft:adventure/craft_decorated_pot_using_only_sherds,minecraft:adventure/trade_at_world_height,minecraft:nether/ride_strider_in_overworld_lava,minecraft:husbandry/balanced_diet,minecraft:husbandry/bred_all_animals,minecraft:husbandry/complete_catalogue,minecraft:husbandry/froglights,minecraft:husbandry/allay_deliver_item_to_player,minecraft:husbandry/plant_any_sniffer_seed");
        GLITCHED_FLIGHT = b.define("enableFlight", true);
        GLITCHED_SHIELD = b.define("enableShield", true);
        GLITCHED_REVIVE = b.define("enableRevive", true);
        GLITCHED_REMOVAL_RAY = b.define("enableRemovalRay", true);
        GLITCHED_EXECUTION_AURA = b.define("enableExecutionAura", true);
        GLITCHED_NULLIFICATION_AURA = b.define("enableInvulnerabilityNullificationAura", true);
        GLITCHED_MOVEMENT_IMMUNITY = b.define("enableMovementImmunity", true);
        GLITCHED_STATUS_IMMUNITY = b.define("enableNegativeStatusImmunity", true);
        GLITCHED_NIGHT_VISION = b.define("enableNightVision", true);
        GLITCHED_BENEFICIAL_EFFECT_PROTECTION = b.define("protectBeneficialEffects", true);
        GLITCHED_SHIELD_STACKS = b.defineInRange("shieldStacks", 30, 0, 100000);
        GLITCHED_SHIELD_RECHARGE_SECONDS = b.defineInRange("shieldRechargeSecondsPerStack", 10, 1, 86400);
        GLITCHED_SHIELD_DAMAGE_MULTIPLIER = b.comment("0.20 means 80% damage reduction.")
                .defineInRange("shieldDamageTakenMultiplier", 0.20D, 0.0D, 1000.0D);
        GLITCHED_REVIVE_COOLDOWN_SECONDS = b.defineInRange("reviveCooldownSeconds", 300, 0, 864000);
        GLITCHED_REVIVE_INVULN_SECONDS = b.defineInRange("reviveInvulnerabilitySeconds", 5, 0, 3600);
        GLITCHED_REMOVAL_RAY_COOLDOWN_SECONDS = b.defineInRange("removalRayCooldownSeconds", 150, 0, 864000);
        GLITCHED_REMOVAL_RAY_RANGE = b.defineInRange("removalRayRange", 50.0D, 0.0D, 4096.0D);
        GLITCHED_REMOVAL_RAY_RADIUS = b.defineInRange("removalRayRadius", 2.0D, 0.0D, 64.0D);
        GLITCHED_RAY_NORMAL_FRACTION = b.defineInRange("removalRayNormalMaxHealthFraction", 0.65D, 0.0D, 1000000.0D);
        GLITCHED_RAY_PLAYER_FRACTION = b.defineInRange("removalRayPlayerMaxHealthFraction", 0.45D, 0.0D, 1000000.0D);
        GLITCHED_RAY_BOSS_FRACTION = b.defineInRange("removalRayBossMaxHealthFraction", 0.25D, 0.0D, 1000000.0D);
        GLITCHED_EXECUTION_THRESHOLD = b.defineInRange("executionThreshold", 0.10D, 0.0D, 1.0D);
        GLITCHED_NULLIFICATION_RADIUS = b.defineInRange("nullificationRadius", 30.0D, 0.0D, 4096.0D);
        b.pop();

        b.push("unleashed");
        UNLEASHED_RECIPE = b.define("enableRecipe", true);
        UNLEASHED_REQUIRE_GLITCHED_ADVANCEMENT = b.define("requireGlitchedCrownAdvancementForCrafting", true);
        UNLEASHED_FLIGHT = b.define("enableFlight", true);
        UNLEASHED_INVULNERABILITY = b.define("enableAbsoluteInvulnerability", true);
        UNLEASHED_INFINITE_REVIVE = b.define("enableInfiniteRevive", true);
        UNLEASHED_REMOVAL_RAY = b.define("enableRemovalRay", true);
        UNLEASHED_FATE = b.define("enableEndPower", true);
        UNLEASHED_OBLIVION_VEIL = b.define("enableOblivionVeil", true);
        UNLEASHED_ANNIHILATION = b.define("enableAnnihilation", true);
        UNLEASHED_DEFENSE_BYPASS = b.comment("Master switch for Unleashed outgoing defense bypass. Unleashed Unleashed ignores it.")
                .define("enableAbsoluteDefenseBypass", true);
        UNLEASHED_DAMAGE_MODE = b.comment("NORMAL, BYPASS_TAGS, or RAW_HEALTH. RAW_HEALTH preserves the 1.1.12 full-power behavior.")
                .defineEnum("damageMode", UnleashedDamageMode.RAW_HEALTH);
        UNLEASHED_UTILITY_EFFECTS = b.define("enableHiddenUtilityEffects", true);
        UNLEASHED_MOVEMENT_IMMUNITY = b.define("enableMovementImmunity", true);
        UNLEASHED_STATUS_IMMUNITY = b.define("enableNegativeStatusImmunity", true);
        UNLEASHED_BENEFICIAL_EFFECT_PROTECTION = b.define("protectBeneficialEffects", true);
        UNLEASHED_NULLIFICATION_AURA = b.define("enableInvulnerabilityNullificationAura", true);
        UNLEASHED_INTEGRITY_PROTECTION = b.define("enableIntegrityProtection", true);
        UNLEASHED_NULLIFICATION_RADIUS = b.defineInRange("nullificationRadius", 96.0D, 0.0D, 4096.0D);
        UNLEASHED_REMOVAL_RAY_RANGE = b.defineInRange("removalRayRange", 50.0D, 0.0D, 4096.0D);
        UNLEASHED_REMOVAL_RAY_RADIUS = b.defineInRange("removalRayRadius", 2.0D, 0.0D, 64.0D);
        UNLEASHED_REMOVAL_RAY_MAX_HEALTH_MULTIPLIER = b.defineInRange("removalRayMaxHealthMultiplier", 16.0D, 0.0D, 1000000.0D);
        UNLEASHED_REMOVAL_RAY_ERASE_REMAINDER = b.define("removalRayEraseRemainingHealth", true);
        UNLEASHED_ANNIHILATION_RADIUS = b.defineInRange("annihilationRadius", 75.0D, 0.0D, 4096.0D);

        b.push("endPower");
        FATE_HALF_CRIT_CHANCE = b.comment("Legacy/unused since The Crowns 1.4.0; retained for config compatibility.")
                .defineInRange("halfCritChance", 5.0D, -1000000.0D, 1000000.0D);
        FATE_HALF_CRIT_DAMAGE = b.comment("Legacy/unused since The Crowns 1.4.0; retained for config compatibility.")
                .defineInRange("halfCritDamage", 5.0D, -1000000.0D, 1000000.0D);
        FATE_FULL_CRIT_CHANCE = b.comment("Legacy/unused since The Crowns 1.4.0; retained for config compatibility.")
                .defineInRange("fullCritChance", 10.0D, -1000000.0D, 1000000.0D);
        FATE_FULL_CRIT_DAMAGE = b.comment("Legacy/unused since The Crowns 1.4.0; retained for config compatibility.")
                .defineInRange("fullCritDamage", 10.0D, -1000000.0D, 1000000.0D);
        FATE_HALF_DAMAGE_MULTIPLIER = b.defineInRange("halfFinalDamageMultiplier", 1024.0D, 0.0D, 1000000.0D);
        FATE_FULL_DAMAGE_MULTIPLIER = b.defineInRange("fullFinalDamageMultiplier", 32767.0D, 0.0D, 1000000.0D);
        FATE_ARMOR_PIERCE = b.defineInRange("armorPierce", 100.0D, -1000000.0D, 1000000.0D);
        FATE_PROTECTION_SHRED = b.defineInRange("protectionShred", 1.0D, -1000.0D, 1000.0D);
        FATE_HALF_ATTACK_SPEED = b.defineInRange("halfAttackSpeed", 5.0D, -1000000.0D, 1000000.0D);
        FATE_HALF_ATTACK_DAMAGE = b.defineInRange("halfAttackDamage", 50.0D, -1000000.0D, 1000000.0D);
        FATE_FULL_ATTACK_SPEED = b.defineInRange("fullAttackSpeed", 10.0D, -1000000.0D, 1000000.0D);
        FATE_FULL_ATTACK_DAMAGE = b.defineInRange("fullAttackDamage", 100.0D, -1000000.0D, 1000000.0D);
        b.pop();
        b.pop();

        SPEC = b.build();
    }

    private CrownServerConfig() {}
}
