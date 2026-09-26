package com.thecrowns.mixin;

import com.thecrowns.item.GlitchedCrownItem;
import com.thecrowns.logic.CrownIntegrity;
import com.thecrowns.logic.AdvancedCrownLogic;
import com.thecrowns.registry.ModItems;
import com.thecrowns.util.ItemStackCustomData;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import com.thecrowns.logic.NewCrownLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    private ItemStack self() {
        return (ItemStack) (Object) this;
    }

    private boolean block() {
        ItemStack stack = self();
        if (!CrownIntegrity.shouldBlockMutation(stack)) return false;
        CrownIntegrity.onBlockedInterference(stack);
        return true;
    }

    @Inject(method = "canElytraFly", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private void thecrowns$allowAngelicElytraFlight(
            LivingEntity entity,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValueZ() && entity instanceof Player player
                && NewCrownLogic.isWearingAngelic(player)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * The Crown has a real 24-bit-sized durability value for compatibility and
     * display metadata, but ordinary durability mechanics are never allowed to
     * consume it or break the item.
     */
    @Inject(method = "isDamageableItem", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$makeCrownUnbreakable(CallbackInfoReturnable<Boolean> cir) {
        if (self().getItem() instanceof GlitchedCrownItem) {
            cir.setReturnValue(false);
            return;
        }
        ItemStack stack = self();
        if (stack.is(ModItems.CURSED_CROWN.get())
                && ItemStackCustomData.getBoolean(stack, AdvancedCrownLogic.TAG_CURSED_BOUND, false)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "setCount", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockSetCount(int count, CallbackInfo ci) {
        if (block()) ci.cancel();
    }

    @Inject(method = "setDamageValue", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockSetDamage(int damage, CallbackInfo ci) {
        if (block()) ci.cancel();
    }

    @Inject(method = "set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;",
            at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("unchecked")
    private void thecrowns$blockComponentSet(DataComponentType<?> type, Object value,
                                              CallbackInfoReturnable<Object> cir) {
        if (block()) cir.setReturnValue(self().get((DataComponentType<Object>) type));
    }

    @Inject(method = "remove(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;",
            at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("unchecked")
    private void thecrowns$blockComponentRemove(DataComponentType<?> type, CallbackInfoReturnable<Object> cir) {
        if (block()) cir.setReturnValue(self().get((DataComponentType<Object>) type));
    }

    @Inject(method = "applyComponentsAndValidate(Lnet/minecraft/core/component/DataComponentPatch;)V",
            at = @At("HEAD"), cancellable = true)
    private void thecrowns$blockValidatedComponentPatch(DataComponentPatch patch, CallbackInfo ci) {
        if (block()) ci.cancel();
    }

    @Inject(method = "applyComponents(Lnet/minecraft/core/component/DataComponentPatch;)V",
            at = @At("HEAD"), cancellable = true)
    private void thecrowns$blockComponentPatch(DataComponentPatch patch, CallbackInfo ci) {
        if (block()) ci.cancel();
    }

    @Inject(method = "applyComponents(Lnet/minecraft/core/component/DataComponentMap;)V",
            at = @At("HEAD"), cancellable = true)
    private void thecrowns$blockComponentMap(DataComponentMap components, CallbackInfo ci) {
        if (block()) ci.cancel();
    }

    @Inject(method = "enchant", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockEnchant(Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment,
                                        int level, CallbackInfo ci) {
        if (block()) ci.cancel();
    }

    @Inject(method = "setRepairCost", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockRepairCost(int cost, CallbackInfo ci) {
        if (block()) ci.cancel();
    }

    @Inject(method = "setHoverName", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockRename(Component name, CallbackInfoReturnable<ItemStack> cir) {
        if (block()) cir.setReturnValue(self());
    }

    @Inject(method = "resetHoverName", at = @At("HEAD"), cancellable = true, require = 0)
    private void thecrowns$blockResetName(CallbackInfo ci) {
        if (block()) ci.cancel();
    }
}
