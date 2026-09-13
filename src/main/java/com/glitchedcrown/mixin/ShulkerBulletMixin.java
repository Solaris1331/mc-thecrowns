package com.glitchedcrown.mixin;

import com.glitchedcrown.logic.CrownLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Aura of Oblivion denies ShulkerBullet tracking in a wide bubble around the
 * wearer.  This intentionally avoids shadowing private target fields so the
 * hook remains stable in the heavily patched Dragonfyre runtime.
 */
@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletMixin {
    private static final double AURA_PROJECTILE_DENIAL_RADIUS = 256.0D;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, require = 0)
    private void glitchedcrown$denyAuraTracking(CallbackInfo ci) {
        ShulkerBullet self = (ShulkerBullet) (Object) this;
        if (!(self.level() instanceof ServerLevel level)) return;

        double radiusSquared = AURA_PROJECTILE_DENIAL_RADIUS * AURA_PROJECTILE_DENIAL_RADIUS;
        for (ServerPlayer player : level.players()) {
            if (player.isAlive()
                    && CrownLogic.isAuraActive(player)
                    && player.distanceToSqr(self) <= radiusSquared) {
                self.kill();
                ci.cancel();
                return;
            }
        }
    }
}
