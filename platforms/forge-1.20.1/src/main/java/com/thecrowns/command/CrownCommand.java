package com.thecrowns.command;

import com.thecrowns.logic.CrownAdvancementGate;
import com.thecrowns.logic.CrownLogic;
import com.thecrowns.logic.MixinDiagnostics;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Pack-developer/admin diagnostics. Legacy /gt controls remain supported. */
public final class CrownCommand {
    private CrownCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("crown")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("diagnostics")
                        .executes(ctx -> diagnostics(ctx.getSource())))
                .then(Commands.literal("inspect")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> inspect(ctx.getSource(), EntityArgument.getPlayer(ctx, "target"))))));
    }

    private static int diagnostics(CommandSourceStack source) {
        var missing = MixinDiagnostics.missingCriticalMixins();
        source.sendSuccess(() -> Component.literal("[Crown] critical Mixin audit: "
                + (missing.isEmpty() ? "PASS" : "FAIL")), false);
        for (String name : MixinDiagnostics.CRITICAL_MIXINS) {
            source.sendSuccess(() -> MixinDiagnostics.statusLine(name), false);
        }
        return missing.isEmpty() ? 1 : 0;
    }

    private static int inspect(CommandSourceStack source, ServerPlayer player) {
        String type = CrownLogic.isWearingUnleashedUnleashed(player) ? "Unleashed Unleashed Crown"
                : CrownLogic.isWearingConfigurableUnleashed(player) ? "Unleashed Crown"
                : CrownLogic.isWearingGlitched(player) ? "Glitched Crown" : "None";
        source.sendSuccess(() -> Component.literal("[Crown] " + player.getGameProfile().getName() + " | equipped=" + type), false);
        source.sendSuccess(() -> Component.literal("  advancement=" + CrownAdvancementGate.requirementProgress(player)
                + "/" + CrownAdvancementGate.requiredPoints() + " usable=" + CrownAdvancementGate.canUseGlitchedCrown(player)), false);
        source.sendSuccess(() -> Component.literal("  shield=" + CrownLogic.getNerfedBarrierCharges(player)
                + "/" + CrownLogic.glitchedBarrierMax()
                + " enabled=" + CrownLogic.isNerfedShieldEnabled(player)), false);
        source.sendSuccess(() -> Component.literal("  revive=" + CrownLogic.isNerfedReviveEnabled(player)
                + " laser=" + CrownLogic.isNerfedLaserEnabled(player)
                + " laserCooldown=" + CrownLogic.isNerfedLaserCooldownEnabled(player)), false);
        source.sendSuccess(() -> Component.literal("  fatePower=" + CrownLogic.getFatePower(player)
                + " aura=" + CrownLogic.isAuraActive(player)
                + " damageMode=" + CrownLogic.unleashedDamageMode(player)
                + " configImmune=" + CrownLogic.ignoresPackConfig(player)), false);
        return 1;
    }
}
