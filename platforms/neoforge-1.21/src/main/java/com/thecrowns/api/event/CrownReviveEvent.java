package com.thecrowns.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** Fired immediately before a Crown revival. */
public class CrownReviveEvent extends Event implements ICancellableEvent {
    public enum Kind { GLITCHED, UNLEASHED }
    private final ServerPlayer player;
    private final Kind kind;
    public CrownReviveEvent(ServerPlayer player, Kind kind) { this.player = player; this.kind = kind; }
    public ServerPlayer getPlayer() { return player; }
    public Kind getKind() { return kind; }
}
