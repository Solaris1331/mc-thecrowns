package com.thecrowns.mixin;

import com.thecrowns.logic.CrownIntegrity;
import com.thecrowns.logic.CrownLogic;
import com.thecrowns.logic.NewCrownLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 2000)
public abstract class LivingEntityMixin {

    /**
     * Preserve Forge's LivingAttack/LivingHurt offensive hooks (notably
     * Apothic Attributes critical-hit calculation), then divert an Unleashed
     * wearer's resolved hit directly into the target's body health before
     * armor, resistance, absorption and LivingDamage-based shields/caps such
     * as VP Shield/OverShield can rewrite it.
     */
    @Redirect(
            method = "actuallyHurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/ForgeHooks;onLivingHurt(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F",
                    remap = false
            ),
            require = 0
    )
    private float thecrowns$absoluteUnleashedDamage(
        LivingEntity target, DamageSource source, float amount
    ) {
        float resolved = ForgeHooks.onLivingHurt(target, source, amount);
        resolved = NewCrownLogic.applyIronforgedRawDamageRules(target, source, resolved);
        if (CrownLogic.applyResolvedAbsoluteDamage(target, source, resolved)) {
            // Health was already changed through DATA_HEALTH_ID. Returning 0
            // stops vanilla armor/magic/absorption and Forge LivingDamage.
            return 0.0F;
        }
        return resolved;
    }

    /** Apply the Iron Guardian's per-hit limit after Forge and every normal final-damage modifier. */
    @Redirect(
            method = "actuallyHurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/ForgeHooks;onLivingDamage(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F",
                    remap = false
            ),
            require = 0
    )
    private float thecrowns$capIronGuardianFinalDamage(
            LivingEntity target, DamageSource source, float amount
    ) {
        float resolved = ForgeHooks.onLivingDamage(target, source, amount);
        return NewCrownLogic.capIronforgedFinalDamage(target, resolved);
    }

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
                && CrownLogic.isDisallowedCrownEffect(effect.getEffect())) {
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
                && CrownLogic.isDisallowedCrownEffect(effect.getEffect())) {
            if (self instanceof ServerPlayer player && CrownLogic.isUnleashedIntegrityActive(player) && source != null) {
                CrownIntegrity.notePotentialInterferer(player, source);
            }
            ci.cancel();
        }
    }

    @Inject(method = "knockback", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockKnockback(double strength, double x, double z, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (CrownLogic.isMovementLocked(self) || CrownLogic.isFateBound(self)) {
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
        return requestedHealth;
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
