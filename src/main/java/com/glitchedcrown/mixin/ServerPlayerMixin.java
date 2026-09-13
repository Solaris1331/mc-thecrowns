package com.glitchedcrown.mixin;

import com.glitchedcrown.logic.CrownLogic;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents external mods from synchronizing an unauthorized flight-state change. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "onUpdateAbilities", at = @At("HEAD"), require = 0)
    private void glitchedcrown$guardUnleashedFlightState(CallbackInfo ci) {
        CrownLogic.guardUnleashedFlightBeforeSync((ServerPlayer) (Object) this);
    }
}
