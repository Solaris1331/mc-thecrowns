package com.thecrowns.logic;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.registry.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Awards Crown progression advancements that require mod-owned crafting/equip state. */
public final class CrownAdvancements {
    private CrownAdvancements() {}

    public static void awardCrafted(ServerPlayer player, ItemStack result) {
        String path = advancementFor(result);
        if (path == null || player.getServer() == null) return;
        award(player, path, "crafted");
        updateTierSummaries(player);
    }

    /**
     * Special Cursed Crown advancements cannot be expressed reliably as ordinary crafting criteria:
     * the Crown can be created by transforming another Crown and may live in a Curios slot.
     */
    public static void tickSpecial(ServerPlayer player) {
        if (player == null || player.getServer() == null) return;
        // Reconcile old saves and advancements granted by commands/datapacks as well as
        // Crowns crafted through the mod-owned Builder result slot. Builder crafting
        // itself still updates immediately through awardCrafted().
        if (player.tickCount % 20 == 0) updateTierSummaries(player);
        if (AdvancedCrownLogic.hasCursedCrown(player)) {
            award(player, "cursed_crown_acquired", "acquired");
        }
        if (AdvancedCrownLogic.isWearingCursed(player)) {
            award(player, "cursed_crown_worn", "worn");
        }
    }

    /**
     * Controls Crown Builder progression. A completed Cursed Crown worn
     * advancement permanently bypasses every tier restriction for that player.
     */
    public static boolean canCraftTier(ServerPlayer player, int requiredTier) {
        if (player == null || player.getServer() == null) return false;
        if (done(player, "cursed_crown_worn")) return true;
        return switch (requiredTier) {
            case 1 -> true;
            case 2 -> done(player, "tier_i_crowns");
            case 3 -> done(player, "tier_ii_crowns");
            case 4 -> done(player, "tier_iii_crowns");
            default -> false;
        };
    }

    private static void updateTierSummaries(ServerPlayer player) {
        if (done(player, "burning_crown") && done(player, "ironforged_crown") && done(player, "frost_crown")) {
            award(player, "tier_i_crowns", "complete");
        }
        if (done(player, "bloody_crown") && done(player, "darkened_crown") && done(player, "warrior_crown")
                && done(player, "divine_crown")) {
            award(player, "tier_ii_crowns", "complete");
        }
        if (done(player, "crown_of_light") && done(player, "dimensional_crown") && done(player, "angelic_crown")
                && done(player, "temporal_crown")) {
            award(player, "tier_iii_crowns", "complete");
        }
    }

    public static boolean done(ServerPlayer player, String path) {
        Advancement advancement = get(player, path);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public static void award(ServerPlayer player, String path, String criterion) {
        Advancement advancement = get(player, path);
        if (advancement != null) player.getAdvancements().award(advancement, criterion);
    }

    private static Advancement get(ServerPlayer player, String path) {
        if (player.getServer() == null) return null;
        return player.getServer().getAdvancements().getAdvancement(new ResourceLocation(TheCrownsMod.MOD_ID, path));
    }

    private static String advancementFor(ItemStack stack) {
        if (stack.is(ModItems.BURNING_CROWN.get())) return "burning_crown";
        if (stack.is(ModItems.IRONFORGED_CROWN.get())) return "ironforged_crown";
        if (stack.is(ModItems.FROST_CROWN.get())) return "frost_crown";
        if (stack.is(ModItems.BLOODY_CROWN.get())) return "bloody_crown";
        if (stack.is(ModItems.DARKENED_CROWN.get())) return "darkened_crown";
        if (stack.is(ModItems.WARRIOR_CROWN.get())) return "warrior_crown";
        if (stack.is(ModItems.DIVINE_CROWN.get())) return "divine_crown";
        if (stack.is(ModItems.CROWN_OF_LIGHT.get())) return "crown_of_light";
        if (stack.is(ModItems.DIMENSIONAL_CROWN.get())) return "dimensional_crown";
        if (stack.is(ModItems.ANGELIC_CROWN.get())) return "angelic_crown";
        if (stack.is(ModItems.TEMPORAL_CROWN.get())) return "temporal_crown";
        if (stack.is(ModItems.GLITCHED_CROWN.get())) return "glitched_crown";
        if (stack.is(ModItems.UNLEASHED_CROWN.get())) return "forbidden_domain";
        return null;
    }
}
