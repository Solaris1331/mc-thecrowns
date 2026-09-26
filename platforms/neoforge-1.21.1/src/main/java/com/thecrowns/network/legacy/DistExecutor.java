package com.thecrowns.network.legacy;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.function.Supplier;

/** Small side-safe bridge for the former packet callbacks. */
public final class DistExecutor {
    private DistExecutor() {}

    public static void unsafeRunWhenOn(Dist side, Supplier<Runnable> action) {
        if (FMLEnvironment.dist == side) action.get().run();
    }
}
