package com.thecrowns.compat;

import com.thecrowns.TheCrownsMod;
import net.minecraftforge.fml.ModList;

/** Optional client recipe-viewer detection without linking their APIs into common code. */
public final class RecipeViewerCompat {
    private RecipeViewerCompat() {}

    public static boolean hasJei() { return ModList.get().isLoaded("jei"); }
    public static boolean hasEmi() { return ModList.get().isLoaded("emi"); }
    public static boolean hasRei() { return ModList.get().isLoaded("roughlyenoughitems"); }

    public static void logDetected() {
        TheCrownsMod.LOGGER.info("Recipe viewer integration detected: JEI={}, EMI={}, REI={}",
                hasJei(), hasEmi(), hasRei());
    }
}
