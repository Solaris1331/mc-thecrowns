package com.thecrowns.mixin;

import com.thecrowns.logic.CrownIntegrity;
import com.thecrowns.logic.CrownLogic;
import com.thecrowns.logic.NewCrownLogic;
import com.thecrowns.logic.ShadowAbyssalLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 2000)
public abstract class LivingEntityMixin {

    @Inject(method = "hurt", at = @At("HEAD"), require = 0)
    private void thecrowns$capturePotentialInterferer(
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> cir
    ) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer player) || !CrownLogic.isUnleashedIntegrityActive(player)) return;

        Entity responsible = source.getEntity();
        if (responsible == null) responsible = source.getDirectEntity();
        if (responsible != null) CrownIntegrity.notePotentialInterferer(player, responsible);
    }

    @Inject(
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void thecrowns$blockHarmfulEffects(
            MobEffectInstance effect,
            Entity source,
            CallbackInfoReturnable<Boolean> cir
    ) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (CrownLogic.hasStatusImmunity(self)
                && CrownLogic.isDisallowedCrownEffect(effect.getEffect().value())) {
            if (self instanceof ServerPlayer player && CrownLogic.isUnleashedIntegrityActive(player) && source != null) {
                CrownIntegrity.notePotentialInterferer(player, source);
            }
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "forceAddEffect",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void thecrowns$blockForcedGravityEffects(
            MobEffectInstance effect,
            Entity source,
            CallbackInfo ci
    ) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (CrownLogic.hasStatusImmunity(self)
                && CrownLogic.isDisallowedCrownEffect(effect.getEffect().value())) {
            if (self instanceof ServerPlayer player && CrownLogic.isUnleashedIntegrityActive(player) && source != null) {
                CrownIntegrity.notePotentialInterferer(player, source);
            }
            ci.cancel();
        }
    }

    @Inject(method = "knockback", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockKnockback(double strength, double x, double z, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (CrownLogic.isMovementLocked(self) || ShadowAbyssalLogic.hasShadowHook(self) || CrownLogic.isFateBound(self)) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "setHealth", at = @At("HEAD"), argsOnly = true, require = 0)
    private float thecrowns$preventHealthLoss(float requestedHealth) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player) {
            if (CrownLogic.hasAnyDamageInvulnerability(player)) {
                return Math.max(requestedHealth, player.getMaxHealth());
            }
            return NewCrownLogic.protectSetHealth(player, requestedHealth);
        }
        return CrownLogic.clampSetHealth(self, requestedHealth);
    }

    @Inject(method = "isDamageSourceBlocked", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$bypassShieldBlocking(
            DamageSource source,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Entity attacker = source.getEntity();
        if (!(attacker instanceof ServerPlayer)) attacker = source.getDirectEntity();
        if (attacker instanceof ServerPlayer player && CrownLogic.isUnleashedDefenseBypassActive(player)) {
            cir.setReturnValue(false);
        }
    }

}
