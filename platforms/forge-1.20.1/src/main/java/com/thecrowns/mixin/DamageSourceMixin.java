package com.thecrowns.mixin;

import com.thecrowns.logic.CrownLogic;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSource.class)
public abstract class DamageSourceMixin {
    private boolean thecrowns$fromCrownWearer() {
        DamageSource self = (DamageSource) (Object) this;
        Entity attacker = self.getEntity();
        return attacker instanceof ServerPlayer player && CrownLogic.isUnleashedDefenseBypassActive(player);
    }

    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"),
            cancellable = true, require = 0)
    private void thecrowns$voidTags(TagKey<DamageType> tag,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (!thecrowns$fromCrownWearer()) return;
        if (DamageTypeTags.BYPASSES_INVULNERABILITY.equals(tag)
                || DamageTypeTags.BYPASSES_ARMOR.equals(tag)
                || DamageTypeTags.BYPASSES_EFFECTS.equals(tag)
                || DamageTypeTags.BYPASSES_RESISTANCE.equals(tag)
                || DamageTypeTags.BYPASSES_ENCHANTMENTS.equals(tag)
                || DamageTypeTags.BYPASSES_COOLDOWN.equals(tag)
                || DamageTypeTags.BYPASSES_SHIELD.equals(tag)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "is(Lnet/minecraft/resources/ResourceKey;)Z", at = @At("HEAD"),
            cancellable = true, require = 0)
    private void thecrowns$voidType(ResourceKey<DamageType> type,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (thecrowns$fromCrownWearer() && DamageTypes.FELL_OUT_OF_WORLD.equals(type)) {
            cir.setReturnValue(true);
        }
    }
}
