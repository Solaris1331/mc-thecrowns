package com.thecrowns.mixin;

import com.thecrowns.logic.CrownIntegrity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemStackHandler.class, remap = false)
public abstract class ItemStackHandlerMixin {
    private ItemStackHandler self() {
        return (ItemStackHandler) (Object) this;
    }

    private ItemStack existing(int slot) {
        ItemStackHandler handler = self();
        if (slot < 0 || slot >= handler.getSlots()) return ItemStack.EMPTY;
        return handler.getStackInSlot(slot);
    }

    @Inject(method = "setStackInSlot", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockCurioReplacement(int slot, ItemStack stack, CallbackInfo ci) {
        ItemStack old = existing(slot);
        if (CrownIntegrity.shouldBlockRemoval(old)) {
            CrownIntegrity.onBlockedInterference(old);
            ci.cancel();
        }
    }

    @Inject(method = "extractItem", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockCurioExtraction(int slot, int amount, boolean simulate,
                                                    CallbackInfoReturnable<ItemStack> cir) {
        ItemStack old = existing(slot);
        if (CrownIntegrity.shouldBlockRemoval(old)) {
            CrownIntegrity.onBlockedInterference(old);
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
    @Inject(method = "setSize", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockHandlerShrink(int size, CallbackInfo ci) {
        ItemStackHandler handler = self();
        int oldSize = handler.getSlots();
        if (size >= oldSize) return;
        for (int slot = Math.max(0, size); slot < oldSize; slot++) {
            ItemStack old = handler.getStackInSlot(slot);
            if (CrownIntegrity.shouldBlockRemoval(old)) {
                CrownIntegrity.onBlockedInterference(old);
                ci.cancel();
                return;
            }
        }
    }

}
