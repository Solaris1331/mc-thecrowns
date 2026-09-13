package com.glitchedcrown.mixin;

import com.glitchedcrown.logic.CrownLogic;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Redirect(
            method = "explode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
            ),
            require = 0
    )
    private void glitchedcrown$cancelExplosionVelocity(Entity entity, Vec3 velocity) {
        if (!CrownLogic.isMovementLocked(entity)) entity.setDeltaMovement(velocity);
    }
}
