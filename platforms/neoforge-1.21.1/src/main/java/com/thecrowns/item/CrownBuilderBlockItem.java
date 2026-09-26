package com.thecrowns.item;

import com.thecrowns.block.CrownBuilderBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Block item for a Crown Builder. The tier is encoded by the backing block. */
public final class CrownBuilderBlockItem extends BlockItem {
    public CrownBuilderBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public int getBuilderTier() {
        return getBlock() instanceof CrownBuilderBlock builder ? builder.getTier() : 0;
    }

    public static int tierOf(ItemStack stack) {
        return stack.getItem() instanceof CrownBuilderBlockItem item ? item.getBuilderTier() : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        int tier = Math.max(1, tierOf(stack));
        tooltip.add(Component.translatable("tooltip.thecrowns.builder.header", roman(tier)).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.thecrowns.builder.workstation").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.thecrowns.builder.blast_simple").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.thecrowns.builder.mine.t" + tier).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.thecrowns.builder.protection.t" + tier).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.thecrowns.builder.craftable.t" + tier).withStyle(ChatFormatting.AQUA));
    }

    private static String roman(int tier) {
        return switch (tier) { case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; default -> "I"; };
    }
}
