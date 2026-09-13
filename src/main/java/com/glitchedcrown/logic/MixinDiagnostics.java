package com.glitchedcrown.logic;

import com.glitchedcrown.GlitchedCrownMod;
import com.glitchedcrown.mixin.CrownMixinPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** Runtime audit for hooks that protect Crown ownership, death retention, and packet boundaries. */
public final class MixinDiagnostics {
    public static final List<String> CRITICAL_MIXINS = List.of(
            "ForgeHooksMixin",
            "CuriosDynamicStackHandlerMixin",
            "ServerGamePacketListenerImplMixin",
            "InventoryMenuArmorSlotMixin",
            "ItemStackHandlerMixin",
            "InventoryMixin",
            "PlayerMixin"
    );

    private static final AtomicBoolean AUDITED = new AtomicBoolean();

    private MixinDiagnostics() {}

    public static void auditAfterPlayerJoin(ServerPlayer player) {
        if (player == null || player.tickCount < 20 || !AUDITED.compareAndSet(false, true)) return;
        ensureLazyCriticalTargetsLoaded();
        List<String> missing = missingCriticalMixins();
        if (missing.isEmpty()) {
            GlitchedCrownMod.LOGGER.info("Critical Crown Mixin audit passed: {}", String.join(", ", CRITICAL_MIXINS));
        } else {
            GlitchedCrownMod.LOGGER.error("CRITICAL Crown Mixin audit failed. Missing transformations: {}. "
                    + "Cursed Crown locking or death retention may be unsafe; do not ship this modpack build.", String.join(", ", missing));
        }
    }

    public static List<String> missingCriticalMixins() {
        ensureLazyCriticalTargetsLoaded();
        return CRITICAL_MIXINS.stream().filter(name -> !CrownMixinPlugin.wasApplied(name)).toList();
    }

    public static Component statusLine(String name) {
        ensureLazyCriticalTargetsLoaded();
        boolean applied = CrownMixinPlugin.wasApplied(name);
        return Component.literal("  " + name + "=" + (applied ? "APPLIED" : "MISSING"));
    }

    /**
     * A Mixin plugin receives postApply only when the target class is loaded. Curios' dynamic
     * handler and Forge's generic item handler are legitimately lazy, so load those two targets
     * before declaring a transformation missing. This does not instantiate or mutate a handler.
     */
    private static void ensureLazyCriticalTargetsLoaded() {
        ClassLoader loader = MixinDiagnostics.class.getClassLoader();
        for (String target : List.of(
                "net.minecraftforge.items.ItemStackHandler",
                "top.theillusivec4.curios.common.inventory.DynamicStackHandler")) {
            try {
                Class.forName(target, false, loader);
            } catch (ClassNotFoundException | LinkageError error) {
                GlitchedCrownMod.LOGGER.error("Unable to load critical Mixin target {}", target, error);
            }
        }
    }
}
