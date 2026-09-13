package com.glitchedcrown.logic;

import com.glitchedcrown.compat.FTBTeamsCompat;
import com.glitchedcrown.config.CrownServerConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Shared pack-configurable Crown target-protection rules. */
public final class CrownTargeting {
    private static final ConcurrentHashMap<Class<?>, Optional<Method>> TAME_METHOD_CACHE = new ConcurrentHashMap<>();
    private CrownTargeting() {}

    public static boolean isProtectedTarget(ServerPlayer wearer, Entity target) {
        if (wearer == null || target == null || target == wearer) return true;

        // Unleashed Unleashed is deliberately config-immune.  Its safety rules
        // stay at the original/full-power defaults even if a pack disables the
        // configurable Crown's target protections.
        if (CrownLogic.isWearingUnleashedUnleashed(wearer)) {
            return isProtectedTargetFixed(wearer, target);
        }

        if (CrownServerConfig.PROTECT_TAMED.get() && isTamed(target)) return true;
        if (CrownServerConfig.PROTECT_OWNED.get() && isOwned(target)) return true;

        if (CrownServerConfig.PROTECT_SCOREBOARD_TEAMS.get()) {
            try {
                if (wearer.isAlliedTo(target) || target.isAlliedTo(wearer)) return true;
            } catch (RuntimeException ignored) {}
        }

        return CrownServerConfig.PROTECT_FTB_TEAMS.get()
                && target instanceof ServerPlayer other
                && FTBTeamsCompat.sameTeam(wearer, other);
    }

    private static boolean isProtectedTargetFixed(ServerPlayer wearer, Entity target) {
        if (isTamed(target) || isOwned(target)) return true;
        try {
            if (wearer.isAlliedTo(target) || target.isAlliedTo(wearer)) return true;
        } catch (RuntimeException ignored) {}
        return target instanceof ServerPlayer other && FTBTeamsCompat.sameTeam(wearer, other);
    }

    public static boolean isTamedOrOwned(Entity entity) {
        return isTamed(entity) || isOwned(entity);
    }

    public static boolean isTamed(Entity entity) {
        if (entity == null) return false;
        if (entity instanceof TamableAnimal tameable) {
            try { if (tameable.isTame()) return true; } catch (RuntimeException ignored) {}
        }
        Optional<Method> probe = TAME_METHOD_CACHE.computeIfAbsent(entity.getClass(), CrownTargeting::findTameProbe);
        if (probe.isPresent()) {
            try {
                Object value = probe.get().invoke(entity);
                if (value instanceof Boolean b) return b;
            } catch (ReflectiveOperationException | RuntimeException ignored) {}
        }
        return false;
    }

    public static boolean isOwned(Entity entity) {
        if (entity == null) return false;
        if (entity instanceof OwnableEntity ownable) {
            try { return ownable.getOwnerUUID() != null; } catch (RuntimeException ignored) {}
        }
        return false;
    }

    private static Optional<Method> findTameProbe(Class<?> type) {
        for (String name : new String[]{"isTame", "isTamed"}) {
            try {
                Method method = type.getMethod(name);
                Class<?> ret = method.getReturnType();
                if (method.getParameterCount() == 0 && (ret == boolean.class || ret == Boolean.class)) {
                    return Optional.of(method);
                }
            } catch (NoSuchMethodException | SecurityException ignored) {}
        }
        return Optional.empty();
    }

    public static boolean isSameFtbTeam(ServerPlayer first, ServerPlayer second) {
        return CrownServerConfig.PROTECT_FTB_TEAMS.get() && FTBTeamsCompat.sameTeam(first, second);
    }
}
