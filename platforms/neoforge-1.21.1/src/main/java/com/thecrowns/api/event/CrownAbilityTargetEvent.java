package com.thecrowns.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired before an offensive Crown ability selects a target. Pack mods may
 * cancel this event to protect custom bosses/NPCs without patching Crown code.
 */
public class CrownAbilityTargetEvent extends Event implements ICancellableEvent {
    public enum Ability { REMOVAL_RAY, EXECUTION, ANNIHILATION, FATE_BIND, NULLIFICATION, ABSOLUTE_DAMAGE, INTEGRITY_RETALIATION }

    private final ServerPlayer wearer;
    private final Entity target;
    private final Ability ability;

    public CrownAbilityTargetEvent(ServerPlayer wearer, Entity target, Ability ability) {
        this.wearer = wearer;
        this.target = target;
        this.ability = ability;
    }

    public ServerPlayer getWearer() { return wearer; }
    public Entity getTarget() { return target; }
    public Ability getAbility() { return ability; }
}
