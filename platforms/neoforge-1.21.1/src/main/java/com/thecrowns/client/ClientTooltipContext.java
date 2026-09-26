package com.thecrowns.client;

import com.thecrowns.logic.CrownLogic;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** Keeps client-only tooltip state out of item classes that are loaded on dedicated servers. */
public final class ClientTooltipContext {
    private ClientTooltipContext() {}

    public static boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    public static void appendCursedHints(List<Component> tooltip) {
        Player viewer = Minecraft.getInstance().player;
        if (viewer != null && viewer.isCreative()) {
            tooltip.add(Component.translatable("tooltip.thecrowns.cursed_crown.passive.creative_hint")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (viewer != null && (CrownLogic.isWearingGlitched(viewer) || CrownLogic.isWearingUnleashed(viewer))) {
            tooltip.add(Component.translatable("tooltip.thecrowns.cursed_crown.passive.crown_hint")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
