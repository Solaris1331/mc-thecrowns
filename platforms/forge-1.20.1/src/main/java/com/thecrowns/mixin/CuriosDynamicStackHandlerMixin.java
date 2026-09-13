package com.thecrowns.mixin;

import com.thecrowns.logic.CrownIntegrity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.common.inventory.DynamicStackHandler;

@Mixin(value = DynamicStackHandler.class, remap = false)
public abstract class CuriosDynamicStackHandlerMixin {
    @Inject(method = "shrink", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockCuriosSlotShrink(int amount, CallbackInfo ci) {
        if (amount <= 0) return;
        ItemStackHandler handler = (ItemStackHandler) (Object) this;
        int oldSize = handler.getSlots();
        int newSize = Math.max(0, oldSize - amount);
        for (int slot = newSize; slot < oldSize; slot++) {
            ItemStack old = handler.getStackInSlot(slot);
            if (CrownIntegrity.shouldBlockRemoval(old)) {
                CrownIntegrity.onBlockedInterference(old);
                ci.cancel();
                return;
            }
        }
    }
}
