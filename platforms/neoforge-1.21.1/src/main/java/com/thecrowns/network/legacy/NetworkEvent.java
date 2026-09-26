package com.thecrowns.network.legacy;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Minimal compatibility context used while the Crown handlers retain their proven server logic. */
public final class NetworkEvent {
    private NetworkEvent() {}

    public static final class Context {
        private final IPayloadContext delegate;

        public Context(IPayloadContext delegate) {
            this.delegate = delegate;
        }

        public void enqueueWork(Runnable task) {
            delegate.enqueueWork(task);
        }

        public ServerPlayer getSender() {
            return delegate.player() instanceof ServerPlayer player ? player : null;
        }

        public void setPacketHandled(boolean ignored) {
            // NeoForge 1.21 acknowledges registered payloads automatically.
        }
    }
}
