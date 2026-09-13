package com.thecrowns.mixin;

import com.thecrowns.logic.CrownLogic;
import com.thecrowns.logic.NewCrownLogic;
import com.thecrowns.logic.AdvancedCrownLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$overrideInvulnerability(
            DamageSource source,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Entity self = (Entity) (Object) this;

        if (self instanceof ServerPlayer player && CrownLogic.hasAnyDamageInvulnerability(player)) {
            cir.setReturnValue(true);
            return;
        }

        if (CrownLogic.shouldStripInvulnerability(self)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "push(DDD)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$cancelExternalPush(double x, double y, double z, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if ((self instanceof ServerPlayer player && NewCrownLogic.shouldCancelWarriorKnockback(player))
                || AdvancedCrownLogic.isTimeStopped(self)
                || CrownLogic.isMovementLocked(self) || CrownLogic.isFateBound(self)) ci.cancel();
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$cancelEntityPush(Entity other, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if ((self instanceof ServerPlayer player && NewCrownLogic.shouldCancelWarriorKnockback(player))
                || AdvancedCrownLogic.isTimeStopped(self)
                || CrownLogic.isMovementLocked(self) || CrownLogic.isFateBound(self)) ci.cancel();
    }

    @Inject(method = "setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$cancelCataclysmVelocityWrite(Vec3 velocity, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if ((AdvancedCrownLogic.isTimeStopped(self) && velocity.lengthSqr() > 1.0E-10D)
                || (CrownLogic.isFateBound(self) && velocity.lengthSqr() > 1.0E-10D)
                || (CrownLogic.isMovementLocked(self) && CrownLogic.isKnownForcedMovementCall())) {
            ci.cancel();
        }
    }

    @Inject(method = "addDeltaMovement", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$cancelCataclysmVelocityAddition(Vec3 velocity, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if ((AdvancedCrownLogic.isTimeStopped(self) && velocity.lengthSqr() > 1.0E-10D)
                || (CrownLogic.isFateBound(self) && velocity.lengthSqr() > 1.0E-10D)
                || (CrownLogic.isMovementLocked(self) && CrownLogic.isKnownForcedMovementCall())) {
            ci.cancel();
        }
    }
    @Inject(method = "move", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$freezeMovement(MoverType moverType, Vec3 movement, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (movement.lengthSqr() <= 1.0E-10D) return;
        if (AdvancedCrownLogic.isTimeStopped(self) || CrownLogic.isFateBound(self)
                || (CrownLogic.isMovementLocked(self) && CrownLogic.isKnownForcedMovementCall())) {
            ci.cancel();
        }
    }

    @Inject(method = "setPos(DDD)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$freezeSetPos(double x, double y, double z, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        double dx = x - self.getX(), dy = y - self.getY(), dz = z - self.getZ();
        if (dx * dx + dy * dy + dz * dz <= 1.0E-10D) return;
        if (AdvancedCrownLogic.isTimeStopped(self) || CrownLogic.isFateBound(self)
                || (CrownLogic.isMovementLocked(self) && CrownLogic.isKnownForcedMovementCall())) {
            ci.cancel();
        }
    }

    @Inject(method = "setPosRaw(DDD)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$freezeSetPosRaw(double x, double y, double z, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        double dx = x - self.getX(), dy = y - self.getY(), dz = z - self.getZ();
        if (dx * dx + dy * dy + dz * dz <= 1.0E-10D) return;
        if (AdvancedCrownLogic.isTimeStopped(self) || CrownLogic.isFateBound(self)
                || (CrownLogic.isMovementLocked(self) && CrownLogic.isKnownForcedMovementCall())) {
            ci.cancel();
        }
    }

    @Inject(method = "teleportTo(DDD)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$freezeTeleport(double x, double y, double z, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (AdvancedCrownLogic.isTimeStopped(self) || CrownLogic.isFateBound(self)
                || (CrownLogic.isMovementLocked(self) && CrownLogic.isKnownForcedMovementCall())) {
            ci.cancel();
        }
    }

}
