package com.thecrowns.mixin.client;

import com.thecrowns.logic.CrownLogic;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Reveals the restored Crown advancement title/description only to a client that owns a Crown. */
@Mixin(DisplayInfo.class)
public abstract class AdvancementDisplayInfoMixin {
    private static final String HIDDEN_TITLE_SENTINEL = "CROWNLOCK!";
    private static final String HIDDEN_DESCRIPTION_SENTINEL = "CROWNDESC!";

    private static boolean ownsCrown() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null && CrownLogic.ownsCrown(minecraft.player);
    }

    @Inject(method = "getTitle", at = @At("RETURN"), cancellable = true, require = 0)
    private void thecrowns$revealForbiddenDomainTitle(CallbackInfoReturnable<Component> cir) {
        Component original = cir.getReturnValue();
        if (original == null || !HIDDEN_TITLE_SENTINEL.equals(original.getString()) || !ownsCrown()) return;
        cir.setReturnValue(Component.translatable("advancement.thecrowns.forbidden_domain.revealed_title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Inject(method = "getDescription", at = @At("RETURN"), cancellable = true, require = 0)
    private void thecrowns$revealForbiddenDomainDescription(CallbackInfoReturnable<Component> cir) {
        Component original = cir.getReturnValue();
        if (original == null || !HIDDEN_DESCRIPTION_SENTINEL.equals(original.getString()) || !ownsCrown()) return;
        cir.setReturnValue(Component.translatable("advancement.thecrowns.forbidden_domain.description")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
