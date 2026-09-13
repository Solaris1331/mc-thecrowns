package com.glitchedcrown.logic;

import com.glitchedcrown.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side integrity guard for an equipped Unleashed Crown.
 *
 * The crown's exact serialized ItemStack state is snapshotted at equip time and
 * becomes immutable until the wearer personally removes it through a normal
 * container-click packet.  Direct ItemStack mutation hooks and Curios handler
 * hooks cancel common mutation/removal paths immediately, while the tick-time
 * snapshot is the fail-safe for mods which write NBT or swap stacks through a
 * path that does not pass those hooks.
 */
public final class CrownIntegrity {
    private static final String STACK_GUARD_ID = "glitchedcrown_guard_id";

    private static final String NBT_ACTIVE = "glitchedcrown_integrity_active";
    private static final String NBT_GUARD_ID = "glitchedcrown_integrity_guard_id";
    private static final String NBT_SNAPSHOT = "glitchedcrown_integrity_snapshot";
    private static final String NBT_SLOT_KIND = "glitchedcrown_integrity_slot_kind";
    private static final String NBT_SLOT_ID = "glitchedcrown_integrity_slot_id";
    private static final String NBT_SLOT_INDEX = "glitchedcrown_integrity_slot_index";

    private static final String SLOT_HELMET = "helmet";
    private static final String SLOT_CURIO = "curio";

    private static final Map<UUID, GuardSession> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> ACTIVE_GUARDS = new ConcurrentHashMap<>();
    private static final Map<UUID, WeakReference<ServerPlayer>> ONLINE_WEARERS = new ConcurrentHashMap<>();
    private static final Map<UUID, RecentInterferer> RECENT_INTERFERERS = new ConcurrentHashMap<>();

    private static final ThreadLocal<UUID> MANUAL_INVENTORY_ACTOR = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> RESTORING = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Entity> ENTITY_CONTEXT = new ThreadLocal<>();

    private CrownIntegrity() {
    }

    public static void tick(ServerPlayer player) {
        ONLINE_WEARERS.put(player.getUUID(), new WeakReference<>(player));
        GuardSession session = session(player);

        if (session == null) {
            EquippedCrown equipped = findAnyEquippedCrown(player);
            if (equipped != null) startSession(player, equipped);
            return;
        }

        ACTIVE_GUARDS.put(session.guardId, player.getUUID());
        if (ensureSessionUnbreakable(session)) persistSession(player, session);

        EquippedCrown guarded = findGuardedEquippedCrown(player, session.guardId);
        if (guarded == null) {
            // Only a player-originated container click is allowed to make the crown
            // disappear from an equipped slot.  That path clears the session in
            // finishManualInventoryAction before this tick runs.
            restore(player, session);
            punishRecentInterferer(player);
            return;
        }

        if (!guarded.location.equals(session.location)
                || !sameState(guarded.stack, session.snapshot)) {
            restore(player, session);
            punishRecentInterferer(player);
            return;
        }

        if (purgeExtraneousGuardCopies(player, session.guardId, guarded.location, guarded.stack)) {
            punishRecentInterferer(player);
        }
    }

    public static void beginManualInventoryAction(ServerPlayer player) {
        MANUAL_INVENTORY_ACTOR.set(player.getUUID());
    }

    public static void finishManualInventoryAction(ServerPlayer player) {
        try {
            GuardSession session = session(player);
            if (session == null) return;

            EquippedCrown guarded = findGuardedEquippedCrown(player, session.guardId);
            if (guarded == null) {
                // This is the one explicitly permitted state transition: the wearer
                // personally removed the crown from an equipped slot.
                clearSession(player, session);
                return;
            }

            // Moving the crown from helmet <-> Curios while keeping it equipped is
            // also a wearer action.  Preserve the canonical state, but follow the
            // new equipped slot.
            session.location = guarded.location;
            persistSession(player, session);

            if (!sameState(guarded.stack, session.snapshot)) {
                restore(player, session);
            }
        } finally {
            MANUAL_INVENTORY_ACTOR.remove();
        }
    }

    public static boolean isManualInventoryAction(UUID owner) {
        UUID current = MANUAL_INVENTORY_ACTOR.get();
        return current != null && current.equals(owner);
    }

    public static boolean isRestoring() {
        return Boolean.TRUE.equals(RESTORING.get());
    }

    /**
     * Returns the wearer UUID only when this exact crown belongs to an active
     * integrity session.  A stale guard id on an unequipped crown is inert.
     */
    public static UUID protectedOwner(ItemStack stack) {
        if (!isCrownStack(stack)) return null;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.hasUUID(STACK_GUARD_ID)) return null;
        UUID guardId = tag.getUUID(STACK_GUARD_ID);
        return ACTIVE_GUARDS.get(guardId);
    }

    public static boolean shouldBlockMutation(ItemStack stack) {
        if (isRestoring()) return false;
        UUID owner = protectedOwner(stack);
        if (owner == null || isManualInventoryAction(owner)) return false;

        // In an integrated game the physical client and server share these static
        // maps. Never freeze a synced client-side copy used by rendering/tooltips;
        // protection is authoritative on the server thread only.
        ServerPlayer wearer = findOnlinePlayer(owner);
        return wearer != null && CrownLogic.isUnleashedIntegrityActive(wearer)
                && wearer.getServer() != null && wearer.getServer().isSameThread();
    }

    public static boolean shouldBlockRemoval(ItemStack existing) {
        return shouldBlockMutation(existing);
    }

    public static void onBlockedInterference(ItemStack stack) {
        UUID owner = protectedOwner(stack);
        if (owner == null) return;
        ServerPlayer player = findOnlinePlayer(owner);
        if (player == null) return;

        Entity context = ENTITY_CONTEXT.get();
        if (context != null && context != player && !context.isRemoved()) {
            Entity responsible = resolveResponsibleEntity(context);
            if (responsible != null && responsible != player && !responsible.isRemoved()) {
                if (CrownLogic.canAffectCrownTarget(player, responsible,
                        com.glitchedcrown.api.event.CrownAbilityTargetEvent.Ability.INTEGRITY_RETALIATION)) {
                    CrownLogic.forceKillInterferer(responsible);
                }
                RECENT_INTERFERERS.remove(owner);
                return;
            }
        }
        punishRecentInterferer(player);
    }

    /**
     * Establishes a best-effort "which entity is currently executing" context.
     * ServerLevelMixin wraps the full non-passenger entity tick with this.
     */
    public static void enterEntityContext(Entity entity) {
        ENTITY_CONTEXT.set(entity);
    }

    public static void exitEntityContext(Entity entity) {
        if (ENTITY_CONTEXT.get() == entity) ENTITY_CONTEXT.remove();
    }

    public static void notePotentialInterferer(ServerPlayer wearer, Entity source) {
        if (wearer == null || source == null || source == wearer || !CrownLogic.isUnleashedIntegrityActive(wearer)) return;

        Entity resolved = resolveResponsibleEntity(source);
        if (resolved == null || resolved == wearer || CrownLogic.isProtectedCrownTarget(wearer, resolved)) return;
        RECENT_INTERFERERS.put(wearer.getUUID(),
                new RecentInterferer(new WeakReference<>(resolved), wearer.level().getGameTime()));
    }

    private static Entity resolveResponsibleEntity(Entity source) {
        if (source instanceof Projectile projectile && projectile.getOwner() != null) {
            return projectile.getOwner();
        }
        return source;
    }

    private static void punishRecentInterferer(ServerPlayer wearer) {
        RecentInterferer recent = RECENT_INTERFERERS.get(wearer.getUUID());
        if (recent == null) return;

        long age = wearer.level().getGameTime() - recent.gameTime;
        if (age < 0L || age > 5L) {
            RECENT_INTERFERERS.remove(wearer.getUUID());
            return;
        }

        Entity entity = recent.entity.get();
        RECENT_INTERFERERS.remove(wearer.getUUID());
        if (entity != null && entity != wearer && !entity.isRemoved()
                && CrownLogic.canAffectCrownTarget(wearer, entity,
                com.glitchedcrown.api.event.CrownAbilityTargetEvent.Ability.INTEGRITY_RETALIATION)) {
            CrownLogic.forceKillInterferer(entity);
        }
    }

    private static ServerPlayer findOnlinePlayer(UUID owner) {
        WeakReference<ServerPlayer> reference = ONLINE_WEARERS.get(owner);
        ServerPlayer player = reference == null ? null : reference.get();
        if (player != null && !player.isRemoved()) return player;

        ONLINE_WEARERS.remove(owner);
        RecentInterferer recent = RECENT_INTERFERERS.get(owner);
        if (recent != null) {
            Entity source = recent.entity.get();
            if (source != null && source.level() != null && source.level().getServer() != null) {
                return source.level().getServer().getPlayerList().getPlayer(owner);
            }
        }
        return null;
    }

    private static GuardSession session(ServerPlayer player) {
        GuardSession existing = SESSIONS.get(player.getUUID());
        if (existing != null) return existing;

        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(NBT_ACTIVE) || !data.hasUUID(NBT_GUARD_ID)
                || !data.contains(NBT_SNAPSHOT)) {
            return null;
        }

        String kind = data.getString(NBT_SLOT_KIND);
        CrownLocation location;
        if (SLOT_HELMET.equals(kind)) {
            location = CrownLocation.forHelmet();
        } else if (SLOT_CURIO.equals(kind)) {
            location = CrownLocation.curio(data.getString(NBT_SLOT_ID), data.getInt(NBT_SLOT_INDEX));
        } else {
            clearPersistentSession(player);
            return null;
        }

        GuardSession loaded = new GuardSession(
                data.getUUID(NBT_GUARD_ID),
                data.getCompound(NBT_SNAPSHOT).copy(),
                location
        );
        SESSIONS.put(player.getUUID(), loaded);
        ACTIVE_GUARDS.put(loaded.guardId, player.getUUID());
        return loaded;
    }

    private static void startSession(ServerPlayer player, EquippedCrown equipped) {
        ItemStack stack = equipped.stack;
        UUID guardId = UUID.randomUUID();
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        stack.getOrCreateTag().putUUID(STACK_GUARD_ID, guardId);

        GuardSession session = new GuardSession(
                guardId,
                stack.save(new CompoundTag()),
                equipped.location
        );
        SESSIONS.put(player.getUUID(), session);
        ACTIVE_GUARDS.put(guardId, player.getUUID());
        persistSession(player, session);
    }

    private static boolean ensureSessionUnbreakable(GuardSession session) {
        CompoundTag itemTag = session.snapshot.getCompound("tag");
        if (itemTag.getBoolean("Unbreakable")) return false;
        itemTag.putBoolean("Unbreakable", true);
        session.snapshot.put("tag", itemTag);
        return true;
    }

    private static void persistSession(ServerPlayer player, GuardSession session) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(NBT_ACTIVE, true);
        data.putUUID(NBT_GUARD_ID, session.guardId);
        data.put(NBT_SNAPSHOT, session.snapshot.copy());
        if (session.location.helmet) {
            data.putString(NBT_SLOT_KIND, SLOT_HELMET);
            data.remove(NBT_SLOT_ID);
            data.remove(NBT_SLOT_INDEX);
        } else {
            data.putString(NBT_SLOT_KIND, SLOT_CURIO);
            data.putString(NBT_SLOT_ID, session.location.identifier);
            data.putInt(NBT_SLOT_INDEX, session.location.index);
        }
    }

    private static void clearSession(ServerPlayer player, GuardSession session) {
        SESSIONS.remove(player.getUUID());
        ACTIVE_GUARDS.remove(session.guardId);
        RECENT_INTERFERERS.remove(player.getUUID());
        ONLINE_WEARERS.remove(player.getUUID());
        clearPersistentSession(player);
    }

    private static void clearPersistentSession(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.remove(NBT_ACTIVE);
        data.remove(NBT_GUARD_ID);
        data.remove(NBT_SNAPSHOT);
        data.remove(NBT_SLOT_KIND);
        data.remove(NBT_SLOT_ID);
        data.remove(NBT_SLOT_INDEX);
    }

    private static ItemStack snapshotStack(GuardSession session) {
        return ItemStack.of(session.snapshot.copy());
    }

    private static boolean sameState(ItemStack current, CompoundTag canonicalSnapshot) {
        if (current == null || current.isEmpty()) return false;
        CompoundTag serialized = current.save(new CompoundTag());
        return serialized.equals(canonicalSnapshot);
    }

    private static void restore(ServerPlayer player, GuardSession session) {
        withRestoring(() -> {
            ItemStack displaced = stackAt(player, session.location);
            ItemStack displacedCopy = ItemStack.EMPTY;
            if (!displaced.isEmpty() && !hasGuardId(displaced, session.guardId)) {
                displacedCopy = displaced.copy();
            }

            purgeGuardCopies(player, session.guardId);

            if (!displacedCopy.isEmpty()) {
                player.getInventory().placeItemBackInInventory(displacedCopy);
            }

            ItemStack restored = snapshotStack(session);
            if (!setStackAt(player, session.location, restored)) {
                // If an external mod removed the Curios slot itself, keeping the
                // crown equipped takes priority over preserving that vanished slot.
                ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                if (!helmet.isEmpty() && !hasGuardId(helmet, session.guardId)) {
                    player.getInventory().placeItemBackInInventory(helmet.copy());
                }
                player.setItemSlot(EquipmentSlot.HEAD, restored);
                session.location = CrownLocation.forHelmet();
                persistSession(player, session);
            }
        });
    }


    /**
     * A copy() of the equipped crown also carries the guard UUID.  Treating that
     * duplicate as valid would let an external effect duplicate protected state
     * without changing the original equipped stack, so extra guarded copies are
     * removed as part of the integrity invariant.
     */
    private static boolean purgeExtraneousGuardCopies(ServerPlayer player, UUID guardId,
                                                        CrownLocation keepLocation, ItemStack keepStack) {
        final boolean[] removed = {false};
        withRestoring(() -> {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack != keepStack && hasGuardId(stack, guardId)) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                    removed[0] = true;
                }
            }

            ItemStack carried = player.containerMenu.getCarried();
            if (carried != keepStack && hasGuardId(carried, guardId)) {
                player.containerMenu.setCarried(ItemStack.EMPTY);
                removed[0] = true;
            }

            CuriosApi.getCuriosInventory(player).resolve().ifPresent(inv ->
                    inv.getCurios().forEach((identifier, handler) -> {
                        var stacks = handler.getStacks();
                        for (int i = 0; i < stacks.getSlots(); i++) {
                            ItemStack stack = stacks.getStackInSlot(i);
                            boolean isKeptSlot = !keepLocation.helmet
                                    && keepLocation.identifier.equals(identifier)
                                    && keepLocation.index == i;
                            if (!isKeptSlot && stack != keepStack && hasGuardId(stack, guardId)) {
                                stacks.setStackInSlot(i, ItemStack.EMPTY);
                                removed[0] = true;
                            }
                        }
                    }));
        });
        return removed[0];
    }

    private static void purgeGuardCopies(ServerPlayer player, UUID guardId) {
        // Vanilla inventory, armor and offhand are all addressable through Inventory.
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (hasGuardId(stack, guardId)) player.getInventory().setItem(i, ItemStack.EMPTY);
        }

        ItemStack carried = player.containerMenu.getCarried();
        if (hasGuardId(carried, guardId)) player.containerMenu.setCarried(ItemStack.EMPTY);

        CuriosApi.getCuriosInventory(player).resolve().ifPresent(inv ->
                inv.getCurios().forEach((identifier, handler) -> {
                    var stacks = handler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        if (hasGuardId(stacks.getStackInSlot(i), guardId)) {
                            stacks.setStackInSlot(i, ItemStack.EMPTY);
                        }
                    }
                }));
    }

    private static ItemStack stackAt(ServerPlayer player, CrownLocation location) {
        if (location.helmet) return player.getItemBySlot(EquipmentSlot.HEAD);

        final ItemStack[] result = {ItemStack.EMPTY};
        CuriosApi.getCuriosInventory(player).resolve().ifPresent(inv -> {
            var handler = inv.getCurios().get(location.identifier);
            if (handler == null) return;
            var stacks = handler.getStacks();
            if (location.index >= 0 && location.index < stacks.getSlots()) {
                result[0] = stacks.getStackInSlot(location.index);
            }
        });
        return result[0];
    }

    private static boolean setStackAt(ServerPlayer player, CrownLocation location, ItemStack stack) {
        if (location.helmet) {
            player.setItemSlot(EquipmentSlot.HEAD, stack);
            return true;
        }

        final boolean[] success = {false};
        CuriosApi.getCuriosInventory(player).resolve().ifPresent(inv -> {
            var handler = inv.getCurios().get(location.identifier);
            if (handler == null) return;
            var stacks = handler.getStacks();
            if (location.index >= 0 && location.index < stacks.getSlots()) {
                stacks.setStackInSlot(location.index, stack);
                success[0] = true;
            }
        });
        return success[0];
    }

    private static EquippedCrown findAnyEquippedCrown(ServerPlayer player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (isCrownStack(helmet)) {
            return new EquippedCrown(CrownLocation.forHelmet(), helmet);
        }

        final EquippedCrown[] found = {null};
        CuriosApi.getCuriosInventory(player).resolve().ifPresent(inv -> {
            if (found[0] != null) return;
            for (var entry : inv.getCurios().entrySet()) {
                var stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (isCrownStack(stack)) {
                        found[0] = new EquippedCrown(CrownLocation.curio(entry.getKey(), i), stack);
                        return;
                    }
                }
            }
        });
        return found[0];
    }

    private static EquippedCrown findGuardedEquippedCrown(ServerPlayer player, UUID guardId) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (hasGuardId(helmet, guardId)) {
            return new EquippedCrown(CrownLocation.forHelmet(), helmet);
        }

        final EquippedCrown[] found = {null};
        CuriosApi.getCuriosInventory(player).resolve().ifPresent(inv -> {
            if (found[0] != null) return;
            for (var entry : inv.getCurios().entrySet()) {
                var stacks = entry.getValue().getStacks();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    if (hasGuardId(stack, guardId)) {
                        found[0] = new EquippedCrown(CrownLocation.curio(entry.getKey(), i), stack);
                        return;
                    }
                }
            }
        });
        return found[0];
    }

    private static boolean isCrownStack(ItemStack stack) {
        // ItemStack mutation mixins can run during bootstrap, before DeferredRegister has
        // finished binding the RegistryObject. Never call RegistryObject#get() that early.
        if (stack == null || stack.isEmpty() || !ModItems.UNLEASHED_CROWN.isPresent()) return false;
        if (stack.is(ModItems.UNLEASHED_CROWN.get())) return true;
        return ModItems.UNLEASHED_UNLEASHED_CROWN.isPresent()
                && stack.is(ModItems.UNLEASHED_UNLEASHED_CROWN.get());
    }

    private static boolean hasGuardId(ItemStack stack, UUID guardId) {
        if (!isCrownStack(stack)) return false;
        CompoundTag tag = stack.getTag();
        return tag != null && tag.hasUUID(STACK_GUARD_ID) && guardId.equals(tag.getUUID(STACK_GUARD_ID));
    }

    private static void withRestoring(Runnable action) {
        boolean previous = RESTORING.get();
        RESTORING.set(true);
        try {
            action.run();
        } finally {
            RESTORING.set(previous);
        }
    }

    private static final class GuardSession {
        private final UUID guardId;
        private final CompoundTag snapshot;
        private CrownLocation location;

        private GuardSession(UUID guardId, CompoundTag snapshot, CrownLocation location) {
            this.guardId = guardId;
            this.snapshot = snapshot;
            this.location = location;
        }
    }

    private record CrownLocation(boolean helmet, String identifier, int index) {
        private static CrownLocation forHelmet() {
            return new CrownLocation(true, "", -1);
        }

        private static CrownLocation curio(String identifier, int index) {
            return new CrownLocation(false, identifier, index);
        }
    }

    private record EquippedCrown(CrownLocation location, ItemStack stack) {
    }

    private record RecentInterferer(WeakReference<Entity> entity, long gameTime) {
    }
}
