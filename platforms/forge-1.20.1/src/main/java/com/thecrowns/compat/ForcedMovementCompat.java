package com.thecrowns.compat;

import java.util.Locale;

/** Centralized caller recognition for external forced-movement systems. */
public final class ForcedMovementCompat {
    private ForcedMovementCompat() {}

    public static String identifyExternalForceCaller() {
        for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
            String owner = frame.getClassName();
            String lower = owner.toLowerCase(Locale.ROOT);
            if (owner.startsWith("com.github.L_Ender.cataclysm.")
                    || owner.startsWith("com.github.lender544.cataclysm.")
                    || owner.startsWith("fuzs.mutantmonsters.")
                    || owner.startsWith("dev.xkmc.l2hostility.")
                    || owner.startsWith("com.gametechbc.traveloptics.")
                    || lower.contains("traveloptics") || lower.contains("cataclysm")
                    || lower.contains("supernova") || lower.contains("blackhole") || lower.contains("black_hole")
                    || lower.contains("telekinesis") || lower.contains("arcaneshackle") || lower.contains("arcane_shackle")
                    || (owner.startsWith("io.redspace.ironsspellbooks.") && lower.contains("root"))
                    || lower.contains("nightwarden") || lower.contains("explosion") || lower.contains("explode")) {
                return owner + "#" + frame.getMethodName();
            }
        }
        return null;
    }
}
