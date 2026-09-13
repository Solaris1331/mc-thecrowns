package com.thecrowns.api.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/** Fired immediately before a Crown revival. */
@Cancelable
public class CrownReviveEvent extends Event {
    public enum Kind { GLITCHED, UNLEASHED }
    private final ServerPlayer player;
    private final Kind kind;
    public CrownReviveEvent(ServerPlayer player, Kind kind) { this.player = player; this.kind = kind; }
    public ServerPlayer getPlayer() { return player; }
    public Kind getKind() { return kind; }
}
