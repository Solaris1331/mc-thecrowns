package com.thecrowns.command;

import com.thecrowns.logic.CrownLogic;
import com.thecrowns.logic.CrownCooldowns;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Administrative runtime controls for the nerfed Glitched Crown. */
public final class GTCommand {
    private GTCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("crowns")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("cdr")
                        .executes(ctx -> CrownCommand.resetCooldowns(
                                ctx.getSource(), ctx.getSource().getPlayerOrException(), "all")))
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.literal("canrevive")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> setCanRevive(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "target"),
                                                BoolArgumentType.getBool(ctx, "value")))))
                        .then(Commands.literal("shieldenable")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> setShieldEnabled(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "target"),
                                                BoolArgumentType.getBool(ctx, "value")))))
                        .then(Commands.literal("rivivetimeset")
                                .then(Commands.argument("seconds", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                        .executes(ctx -> setReviveTime(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "target"),
                                                IntegerArgumentType.getInteger(ctx, "seconds")))))
                        .then(Commands.literal("removelaycdset")
                                .then(Commands.argument("seconds", IntegerArgumentType.integer(0, Integer.MAX_VALUE))
                                        .executes(ctx -> setRemoveLayCooldown(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "target"),
                                                IntegerArgumentType.getInteger(ctx, "seconds")))))
                        .then(Commands.literal("canremovelay")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> setCanRemoveLay(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "target"),
                                                BoolArgumentType.getBool(ctx, "value")))))
                        .then(Commands.literal("useremoveraycd")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> setUseRemoveRayCooldown(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "target"),
                                                BoolArgumentType.getBool(ctx, "value")))))
                        .then(Commands.literal("shieldstack")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 100000))
                                                .executes(ctx -> setShieldStack(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "target"),
                                                        IntegerArgumentType.getInteger(ctx, "value")))))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 100000))
                                                .executes(ctx -> addShieldStack(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "target"),
                                                        IntegerArgumentType.getInteger(ctx, "value")))))
                                .then(Commands.literal("max")
                                        .executes(ctx -> setShieldStack(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "target"),
                                                CrownLogic.glitchedBarrierMax())))
                                .then(Commands.literal("rem")
                                        .executes(ctx -> setShieldStack(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "target"),
                                                0))))));
    }

    private static int setCanRevive(CommandSourceStack source, ServerPlayer target, boolean enabled) {
        CrownLogic.setNerfedReviveEnabled(target, enabled);
        source.sendSuccess(() -> Component.literal("[Crowns] " + target.getGameProfile().getName()
                + " Glitched Crown canrevive = " + enabled), true);
        return 1;
    }

    private static int setShieldEnabled(CommandSourceStack source, ServerPlayer target, boolean enabled) {
        CrownLogic.setNerfedShieldEnabled(target, enabled);
        source.sendSuccess(() -> Component.literal("[Crowns] " + target.getGameProfile().getName()
                + " Glitched Crown shieldenable = " + enabled), true);
        return 1;
    }

    private static int setReviveTime(CommandSourceStack source, ServerPlayer target, int seconds) {
        int result = CrownLogic.setNerfedReviveCooldownSeconds(target, seconds);
        source.sendSuccess(() -> Component.literal("[Crowns] " + target.getGameProfile().getName()
                + " revive cooldown remaining = " + result + "s"), true);
        return 1;
    }

    private static int setRemoveLayCooldown(CommandSourceStack source, ServerPlayer target, int seconds) {
        int result = CrownLogic.setNerfedLaserCooldownSeconds(target, seconds);
        source.sendSuccess(() -> Component.literal("[Crowns] " + target.getGameProfile().getName()
                + " deletion beam cooldown remaining = " + result + "s"), true);
        return 1;
    }

    private static int setCanRemoveLay(CommandSourceStack source, ServerPlayer target, boolean enabled) {
        CrownLogic.setNerfedLaserEnabled(target, enabled);
        source.sendSuccess(() -> Component.literal("[Crowns] " + target.getGameProfile().getName()
                + " Glitched Crown canremovelay = " + enabled), true);
        return 1;
    }

    private static int setUseRemoveRayCooldown(CommandSourceStack source, ServerPlayer target, boolean enabled) {
        CrownLogic.setNerfedLaserCooldownEnabled(target, enabled);
        source.sendSuccess(() -> Component.literal("[Crowns] " + target.getGameProfile().getName()
                + " Glitched Crown useremoveraycd = " + enabled), true);
        return 1;
    }

    private static int setShieldStack(CommandSourceStack source, ServerPlayer target, int value) {
        int result = CrownLogic.setNerfedBarrierCharges(target, value);
        if (result < 0) {
            source.sendFailure(Component.literal("[Crowns] " + target.getGameProfile().getName()
                    + " is not wearing a Glitched Crown."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("[Crowns] " + target.getGameProfile().getName()
                + " shield stack = " + result + "/" + CrownLogic.glitchedBarrierMax()), true);
        return 1;
    }

    private static int addShieldStack(CommandSourceStack source, ServerPlayer target, int value) {
        int result = CrownLogic.addNerfedBarrierCharges(target, value);
        if (result < 0) {
            source.sendFailure(Component.literal("[Crowns] " + target.getGameProfile().getName()
                    + " is not wearing a Glitched Crown."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("[Crowns] " + target.getGameProfile().getName()
                + " shield stack = " + result + "/" + CrownLogic.glitchedBarrierMax()), true);
        return 1;
    }
}
