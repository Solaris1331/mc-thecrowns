package com.thecrowns.mixin;

import com.thecrowns.logic.CrownIntegrity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    private Inventory self() {
        return (Inventory) (Object) this;
    }

    private ItemStack existing(int slot) {
        Inventory inventory = self();
        if (slot < 0 || slot >= inventory.getContainerSize()) return ItemStack.EMPTY;
        return inventory.getItem(slot);
    }

    @Inject(method = "setItem", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockExternalSetItem(int slot, ItemStack replacement, CallbackInfo ci) {
        ItemStack old = existing(slot);
        if (CrownIntegrity.shouldBlockRemoval(old)) {
            CrownIntegrity.onBlockedInterference(old);
            ci.cancel();
        }
    }

    @Inject(method = "removeItem(II)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockExternalRemoveItem(int slot, int amount,
                                                       CallbackInfoReturnable<ItemStack> cir) {
        ItemStack old = existing(slot);
        if (CrownIntegrity.shouldBlockRemoval(old)) {
            CrownIntegrity.onBlockedInterference(old);
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "removeItemNoUpdate", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockExternalRemoveItemNoUpdate(int slot,
                                                               CallbackInfoReturnable<ItemStack> cir) {
        ItemStack old = existing(slot);
        if (CrownIntegrity.shouldBlockRemoval(old)) {
            CrownIntegrity.onBlockedInterference(old);
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
