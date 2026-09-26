package com.thecrowns.logic;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.config.CrownServerConfig;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Pack-configurable advancement gates for Crown progression. */
public final class CrownAdvancementGate {
    public static final ResourceLocation GLITCHED_CROWN_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(TheCrownsMod.MOD_ID, "glitched_crown");

    public static final ResourceLocation ALL_POTIONS = ResourceLocation.fromNamespaceAndPath("minecraft", "nether/all_potions");
    public static final ResourceLocation ALL_EFFECTS = ResourceLocation.fromNamespaceAndPath("minecraft", "nether/all_effects");

    /** 1.4.5-1.4.14 untouched default, used only for safe in-memory migration of existing worlds. */
    public static final String LEGACY_DEFAULT_SUBSTITUTES =
            "minecraft:nether/fast_travel,minecraft:nether/uneasy_alliance,minecraft:nether/all_potions,minecraft:nether/all_effects,minecraft:nether/explore_nether,minecraft:end/levitate,minecraft:adventure/kill_all_mobs,minecraft:adventure/sniper_duel,minecraft:adventure/arbalistic,minecraft:adventure/bullseye,minecraft:nether/create_full_beacon,minecraft:adventure/hero_of_the_village";

    /** New 1.4.15 default additions; duplicates from the user's request are intentionally not repeated. */
    public static final List<ResourceLocation> V1_4_15_ADDITIONAL_SUBSTITUTES = List.of(
            ResourceLocation.fromNamespaceAndPath("minecraft", "husbandry/obtain_netherite_hoe"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "adventure/very_very_frightening"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "adventure/craft_decorated_pot_using_only_sherds"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "adventure/trade_at_world_height"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "nether/ride_strider_in_overworld_lava"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "husbandry/balanced_diet"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "husbandry/bred_all_animals"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "husbandry/complete_catalogue"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "husbandry/froglights"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "husbandry/allay_deliver_item_to_player"),
            ResourceLocation.fromNamespaceAndPath("minecraft", "husbandry/plant_any_sniffer_seed")
    );

    public record AdvancementStatus(ResourceLocation id, Component title, boolean done) {}

    private CrownAdvancementGate() {}

    public static boolean canUseGlitchedCrown(ServerPlayer player) {
        if (!CrownServerConfig.GLITCHED_ADVANCEMENT_GATE.get()) return true;
        if (player != null && player.isCreative()) return true;
        if (player == null || player.getServer() == null) return false;
        return requirementProgressRaw(player) >= requiredPoints();
    }

    public static boolean isGlitchedGateEnabled() {
        return CrownServerConfig.GLITCHED_ADVANCEMENT_GATE.get();
    }

    public static void setGlitchedGateEnabled(boolean enabled) {
        CrownServerConfig.GLITCHED_ADVANCEMENT_GATE.set(enabled);
    }

    public static int requiredPoints() {
        return Math.max(0, CrownServerConfig.GLITCHED_ADVANCEMENT_POINTS.get());
    }

    public static List<ResourceLocation> coreAdvancements() {
        return parseDistinct(CrownServerConfig.GLITCHED_CORE_ADVANCEMENTS.get());
    }

    public static List<ResourceLocation> substituteAdvancements() {
        String configured = CrownServerConfig.GLITCHED_SUBSTITUTE_ADVANCEMENTS.get();
        LinkedHashSet<ResourceLocation> ids = new LinkedHashSet<>(parseDistinct(configured));
        // Existing worlds keep their serverconfig file. If it is still exactly the old untouched
        // default, merge the 1.4.15 additions in memory so the update works without regenerating config.
        if (configured != null && configured.trim().equals(LEGACY_DEFAULT_SUBSTITUTES)) {
            ids.addAll(V1_4_15_ADDITIONAL_SUBSTITUTES);
        }
        return List.copyOf(ids);
    }

    public static int completedCore(ServerPlayer player) {
        return completedCount(player, coreAdvancements());
    }

    /** Total completed substitute advancements, independent of their point weighting. */
    public static int completedChallengeSubstitutes(ServerPlayer player) {
        return completedCount(player, substituteAdvancements());
    }

    public static int requirementProgress(ServerPlayer player) {
        return Math.min(requiredPoints(), requirementProgressRaw(player));
    }

    private static int requirementProgressRaw(ServerPlayer player) {
        int full = 0;
        int half = 0;
        for (ResourceLocation id : substituteAdvancements()) {
            if (!isDone(player, id)) continue;
            if (isFullValueSubstitute(id)) full++;
            else half++;
        }
        // all_potions and all_effects each count as a complete substitute point.
        // Every other substitute still requires a pair to equal one core point.
        return completedCore(player) + full + half / 2;
    }

    public static int remainingCoreRequirementCount(ServerPlayer player) {
        return Math.max(0, requiredPoints() - requirementProgressRaw(player));
    }

    public static boolean isFullValueSubstitute(ResourceLocation id) {
        return ALL_POTIONS.equals(id) || ALL_EFFECTS.equals(id);
    }

    public static boolean hasGlitchedCrownAdvancement(ServerPlayer player) {
        return isDone(player, GLITCHED_CROWN_ADVANCEMENT);
    }

    public static boolean isDone(ServerPlayer player, ResourceLocation id) {
        if (player == null || player.getServer() == null || id == null) return false;
        AdvancementHolder advancement = player.getServer().getAdvancements().get(id);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public static List<AdvancementStatus> coreStatus(ServerPlayer player) {
        return status(player, coreAdvancements());
    }

    public static List<AdvancementStatus> substituteStatus(ServerPlayer player) {
        return status(player, substituteAdvancements());
    }

    private static List<AdvancementStatus> status(ServerPlayer player, List<ResourceLocation> ids) {
        List<AdvancementStatus> out = new ArrayList<>(ids.size());
        for (ResourceLocation id : ids) {
            AdvancementHolder advancement = player != null && player.getServer() != null
                    ? player.getServer().getAdvancements().get(id) : null;
            Component title = advancement != null && advancement.value().display().isPresent()
                    ? advancement.value().display().get().getTitle()
                    : Component.literal(id.toString());
            out.add(new AdvancementStatus(id, title, advancement != null && isDone(player, id)));
        }
        return List.copyOf(out);
    }

    private static int completedCount(ServerPlayer player, List<ResourceLocation> ids) {
        int count = 0;
        for (ResourceLocation id : ids) if (isDone(player, id)) count++;
        return count;
    }

    private static List<ResourceLocation> parseDistinct(String csv) {
        Set<ResourceLocation> out = new LinkedHashSet<>();
        if (csv == null || csv.isBlank()) return List.of();
        for (String raw : csv.split(",")) {
            String s = raw.trim();
            if (s.isEmpty()) continue;
            ResourceLocation id = ResourceLocation.tryParse(s);
            if (id != null) out.add(id);
        }
        return List.copyOf(out);
    }
}
