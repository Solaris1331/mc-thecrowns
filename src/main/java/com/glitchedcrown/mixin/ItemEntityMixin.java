package com.glitchedcrown.mixin;

import com.glitchedcrown.item.CrownBuilderBlockItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Physical protection rules for dropped Crown Builder III/IV items. */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow private int age;

    private ItemEntity self() { return (ItemEntity) (Object) this; }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void glitchedcrown$protectBuilderItem(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        int tier = CrownBuilderBlockItem.tierOf(self().getItem());
        if (tier < 3) return;
        // Tier III intentionally remains disposable by the void; Tier IV blocks every ordinary damage source.
        if (tier == 3 && source.is(DamageTypes.FELL_OUT_OF_WORLD)) return;
        cir.setReturnValue(false);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void glitchedcrown$tickProtectedBuilderItem(CallbackInfo ci) {
        ItemEntity entity = self();
        int tier = CrownBuilderBlockItem.tierOf(entity.getItem());
        if (tier < 3) return;

        // No natural despawn and no lingering fire state for Tier III+.
        age = 0;
        entity.clearFire();

        if (tier >= 4) {
            int floor = entity.level().getMinBuildHeight();
            // Rescue slightly above the dimension floor before vanilla below-world removal can run.
            if (entity.getY() <= floor + 2.0D) {
                Vec3 motion = entity.getDeltaMovement();
                entity.setPos(entity.getX(), floor + 3.0D, entity.getZ());
                entity.setDeltaMovement(motion.x, Math.max(0.0D, motion.y), motion.z);
                entity.fallDistance = 0.0F;
            }
        }
    }
}
