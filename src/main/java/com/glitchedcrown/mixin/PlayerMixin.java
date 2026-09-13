package com.glitchedcrown.mixin;

import com.glitchedcrown.logic.CrownIntegrity;
import com.glitchedcrown.logic.NewCrownLogic;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Player-specific equipment replacement guard.
 *
 * LivingEntity#setItemSlot is abstract in this hierarchy, so injecting at HEAD
 * there has no bytecode instruction to target and crashes Mixin during PREINJECT.
 * Player supplies the concrete implementation used for player equipment slots.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true, require = 0)
    private void glitchedcrown$allowAngelicElytraFlight(CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) (Object) this;
        if (!NewCrownLogic.isWearingAngelic(self) || self.onGround() || self.isFallFlying()
                || self.isInWater() || self.hasEffect(MobEffects.LEVITATION)) return;
        self.startFallFlying();
        cir.setReturnValue(true);
    }

    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true, require = 0)
    private void glitchedcrown$blockExternalEquipmentReplacement(
            EquipmentSlot slot,
            ItemStack replacement,
            CallbackInfo ci
    ) {
        if (slot != EquipmentSlot.HEAD) return;

        Player self = (Player) (Object) this;
        ItemStack current = self.getItemBySlot(slot);
        if (CrownIntegrity.shouldBlockRemoval(current)) {
            CrownIntegrity.onBlockedInterference(current);
            ci.cancel();
        }
    }
}
