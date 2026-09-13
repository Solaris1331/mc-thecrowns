package com.glitchedcrown.mixin;

import com.glitchedcrown.logic.AdvancedCrownLogic;
import com.glitchedcrown.logic.CrownLogic;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Crown FORCED_DEATH cannot be canceled by LivingDeathEvent handlers. */
@Mixin(value = ForgeHooks.class, remap = false)
public abstract class ForgeHooksMixin {
    @Inject(method = "onLivingDeath", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void glitchedcrown$forcedDeathCannotBeCanceled(
            LivingEntity entity,
            DamageSource source,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // Run before Forge posts LivingDeathEvent. Grave mods therefore never see the
        // authoritative Temporal/Cursed Crown stacks and cannot move them into a grave.
        // The serialized ticket is consumed exactly once after a canceled death or clone.
        if (entity instanceof ServerPlayer player) {
            AdvancedCrownLogic.prepareCrownDeathRetention(player);
        }
        if (CrownLogic.isForcedDeath(entity)) {
            // ForgeHooks#onLivingDeath returns true to CANCEL death.
            cir.setReturnValue(false);
        }
    }
}
