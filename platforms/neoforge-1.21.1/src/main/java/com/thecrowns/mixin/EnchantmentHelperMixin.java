package com.thecrowns.mixin;

import com.thecrowns.logic.CrownIntegrity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @Inject(method = "setEnchantments", at = @At("HEAD"), cancellable = true, require = 0)
    private static void thecrowns$blockEnchantmentRewrite(ItemStack stack,
                                                           ItemEnchantments enchantments,
                                                               CallbackInfo ci) {
        if (CrownIntegrity.shouldBlockMutation(stack)) {
            CrownIntegrity.onBlockedInterference(stack);
            ci.cancel();
        }
    }
}
