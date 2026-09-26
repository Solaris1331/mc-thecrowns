package com.thecrowns.mixin;

import com.thecrowns.config.CrownServerConfig;
import com.thecrowns.logic.CrownAdvancementGate;
import com.thecrowns.registry.ModItems;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps the Unleashed recipe's per-player advancement gate outside stateless CraftingInput. */
@Mixin(Slot.class)
public abstract class ResultSlotMixin {
    @Inject(method = "mayPickup(Lnet/minecraft/world/entity/player/Player;)Z", at = @At("HEAD"), cancellable = true)
    private void thecrowns$requireGlitchedAdvancement(net.minecraft.world.entity.player.Player player,
                                                      CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ResultSlot
                && !player.level().isClientSide
                && ((Slot) (Object) this).getItem().is(ModItems.UNLEASHED_CROWN.get())
                && CrownServerConfig.UNLEASHED_REQUIRE_GLITCHED_ADVANCEMENT.get()
                && (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                || !CrownAdvancementGate.hasGlitchedCrownAdvancement(serverPlayer))) {
            cir.setReturnValue(false);
        }
    }
}
