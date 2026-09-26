package com.thecrowns.mixin;

import com.thecrowns.logic.CrownIntegrity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "tickNonPassenger", at = @At("HEAD"), require = 0)
    private void thecrowns$enterEntityTick(Entity entity, CallbackInfo ci) {
        CrownIntegrity.enterEntityContext(entity);
    }

    @Inject(method = "tickNonPassenger", at = @At("RETURN"), require = 0)
    private void thecrowns$exitEntityTick(Entity entity, CallbackInfo ci) {
        CrownIntegrity.exitEntityContext(entity);
    }
}
