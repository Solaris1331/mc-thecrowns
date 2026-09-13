package com.thecrowns.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes vanilla DATA_HEALTH so Crown deletion can bypass third-party setHealth() hooks. */
@Mixin(LivingEntity.class)
public interface LivingEntityHealthAccessor {
    @Accessor("DATA_HEALTH_ID")
    static EntityDataAccessor<Float> thecrowns$getDataHealth() {
        throw new AssertionError();
    }
}
