package com.thecrowns.mixin;

import com.thecrowns.logic.AdvancedCrownLogic;
import com.thecrowns.logic.CrownIntegrity;
import com.thecrowns.logic.CrownLogic;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "handlePlayerAbilities", at = @At("RETURN"), require = 0)
    private void thecrowns$authorizePlayerFlightToggle(ServerboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
        CrownLogic.applyAuthorizedPlayerFlightToggle(this.player, packet.isFlying());
    }

    @Inject(method = "handleContainerClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER), cancellable = true, require = 0)
    private void thecrowns$beginWearerInventoryAction(ServerboundContainerClickPacket packet,
                                                           CallbackInfo ci) {
        if (AdvancedCrownLogic.shouldBlockCursedCrownContainerClick(this.player, packet)) {
            // Do not let vanilla/Curios touch the slot at all. A full state push also removes
            // any client-side ghost movement without creating or restoring another stack.
            this.player.containerMenu.broadcastFullState();
            ci.cancel();
            return;
        }
        CrownIntegrity.beginManualInventoryAction(this.player);
    }

    @Inject(method = "handleContainerClick", at = @At("RETURN"), require = 0)
    private void thecrowns$finishWearerInventoryAction(ServerboundContainerClickPacket packet,
                                                            CallbackInfo ci) {
        CrownIntegrity.finishManualInventoryAction(this.player);
        AdvancedCrownLogic.releaseCursedBindingIfAllowed(this.player);
    }

    @Inject(method = "handleSetCreativeModeSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift = At.Shift.AFTER), require = 0)
    private void thecrowns$beginCreativeInventoryAction(ServerboundSetCreativeModeSlotPacket packet,
                                                             CallbackInfo ci) {
        CrownIntegrity.beginManualInventoryAction(this.player);
    }

    @Inject(method = "handleSetCreativeModeSlot", at = @At("RETURN"), require = 0)
    private void thecrowns$finishCreativeInventoryAction(ServerboundSetCreativeModeSlotPacket packet,
                                                              CallbackInfo ci) {
        CrownIntegrity.finishManualInventoryAction(this.player);
        AdvancedCrownLogic.releaseCursedBindingIfAllowed(this.player);
    }
}
