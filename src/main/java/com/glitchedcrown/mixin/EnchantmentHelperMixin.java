package com.glitchedcrown.mixin;

import com.glitchedcrown.logic.CrownIntegrity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @Inject(method = "setEnchantments", at = @At("HEAD"), cancellable = true, require = 0)
    private static void glitchedcrown$blockEnchantmentRewrite(Map<Enchantment, Integer> enchantments,
                                                               ItemStack stack,
                                                               CallbackInfo ci) {
        if (CrownIntegrity.shouldBlockMutation(stack)) {
            CrownIntegrity.onBlockedInterference(stack);
            ci.cancel();
        }
    }
}
