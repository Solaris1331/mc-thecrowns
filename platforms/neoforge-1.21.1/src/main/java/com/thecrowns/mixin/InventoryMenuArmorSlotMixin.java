package com.thecrowns.mixin;

import com.thecrowns.logic.AdvancedCrownLogic;
import com.thecrowns.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla player armor slots are anonymous InventoryMenu slots.  This mirrors
 * the normal Binding Curse pickup check, but with The Crowns' explicit removal
 * exceptions for the Cursed Crown.
 */
@Mixin(targets = "net.minecraft.world.inventory.InventoryMenu$1")
public abstract class InventoryMenuArmorSlotMixin {

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$lockCursedCrown(Player player, CallbackInfoReturnable<Boolean> cir) {
        Slot self = (Slot) (Object) this;
        ItemStack stack = self.getItem();
        if (!stack.isEmpty() && stack.is(ModItems.CURSED_CROWN.get())) {
            if (!AdvancedCrownLogic.canRemoveCursedCrown(player)) {
                cir.setReturnValue(false);
            } else if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                AdvancedCrownLogic.releaseCursedBinding(serverPlayer);
            }
        }
    }
}
