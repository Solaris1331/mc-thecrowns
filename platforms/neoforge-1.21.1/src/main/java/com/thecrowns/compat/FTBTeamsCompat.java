package com.thecrowns.compat;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

/** Optional FTB Teams bridge. No FTB class is linked at compile/class-load time. */
public final class FTBTeamsCompat {
    private static final String API = "dev.ftb.mods.ftbteams.api.FTBTeamsAPI";

    private FTBTeamsCompat() {}

    public static boolean sameTeam(ServerPlayer first, ServerPlayer second) {
        if (first == null || second == null || first == second) return first == second;
        if (!ModList.get().isLoaded("ftbteams")) return false;
        try {
            Class<?> apiClass = Class.forName(API, false, FTBTeamsCompat.class.getClassLoader());
            Method apiMethod = apiClass.getMethod("api");
            Object api = apiMethod.invoke(null);
            if (api == null) return false;
            Class<?> apiInterface = apiMethod.getReturnType();
            try {
                Method loaded = apiInterface.getMethod("isManagerLoaded");
                Object ready = loaded.invoke(api);
                if (ready instanceof Boolean b && !b) return false;
            } catch (NoSuchMethodException ignored) {}
            Method getManager = apiInterface.getMethod("getManager");
            Object manager = getManager.invoke(api);
            if (manager == null) return false;
            Class<?> managerInterface = getManager.getReturnType();
            try {
                Method same = managerInterface.getMethod("arePlayersInSameTeam", UUID.class, UUID.class);
                Object result = same.invoke(manager, first.getUUID(), second.getUUID());
                if (result instanceof Boolean b) return b;
            } catch (NoSuchMethodException ignored) {}
            Method lookup = managerInterface.getMethod("getTeamForPlayerID", UUID.class);
            Object a = lookup.invoke(manager, first.getUUID());
            Object b = lookup.invoke(manager, second.getUUID());
            if (!(a instanceof Optional<?> ao) || !(b instanceof Optional<?> bo) || ao.isEmpty() || bo.isEmpty()) return false;
            Object at = ao.get(), bt = bo.get();
            if (at == bt) return true;
            Class<?> team = Class.forName("dev.ftb.mods.ftbteams.api.Team", false, FTBTeamsCompat.class.getClassLoader());
            Method teamId = team.getMethod("getTeamId");
            Object aid = teamId.invoke(at), bid = teamId.invoke(bt);
            return aid instanceof UUID && aid.equals(bid);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            return false;
        }
    }
}
