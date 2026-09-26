package com.thecrowns.logic;

import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;

public final class CrownCooldowns {
    public static final List<String> NAMES = List.of(
            "all", "light", "bloody", "burning", "darkened", "ironforged", "dimensional",
            "warrior", "angelic", "temporal", "frost", "divine", "cursed", "glitched",
            "unleashed", "unleashed_unleashed"
    );

    private CrownCooldowns() {
    }

    public static boolean reset(ServerPlayer player, String rawName) {
        if (player == null) return false;
        String name = normalize(rawName);
        if (!NAMES.contains(name)) return false;

        if (name.equals("all")) {
            for (String crown : NAMES) {
                if (!crown.equals("all")) resetOne(player, crown);
            }
        } else {
            resetOne(player, name);
        }
        return true;
    }

    private static void resetOne(ServerPlayer player, String crown) {
        NewCrownLogic.resetCooldowns(player, crown);
        AdvancedCrownLogic.resetCooldowns(player, crown);
        if (crown.equals("glitched")) CrownLogic.resetGlitchedCooldowns(player);
    }

    private static String normalize(String rawName) {
        if (rawName == null || rawName.isBlank()) return "all";
        String value = rawName.trim().toLowerCase(Locale.ROOT);
        int namespace = value.indexOf(':');
        if (namespace >= 0) value = value.substring(namespace + 1);
        value = value.replace('-', '_').replace(' ', '_');
        if (value.equals("crown_of_light")) return "light";
        if (value.endsWith("_crown")) value = value.substring(0, value.length() - "_crown".length());
        return value.replace("iron_forged", "ironforged");
    }
}
