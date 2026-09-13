package com.thecrowns.item;

import net.minecraft.ChatFormatting;
import com.thecrowns.client.ClientTooltipContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** The original pre-nerf Crown, renamed to Unleashed Crown. */
public class UnleashedCrownItem extends GlitchedCrownItem {
    public UnleashedCrownItem(ArmorMaterial material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    protected String tooltipRoot() {
        return "tooltip.thecrowns.unleashed";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (!ClientTooltipContext.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.thecrowns.hold_shift")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.thecrowns.unleashed.active.title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        for (int i = 1; i <= 4; i++) {
            tooltip.add(Component.translatable("tooltip.thecrowns.unleashed.active." + i)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.thecrowns.unleashed.keys",
                        Component.keybind("key.thecrowns.fire_laser"),
                        Component.keybind("key.thecrowns.toggle_fate"),
                        Component.keybind("key.thecrowns.toggle_aura"),
                        Component.keybind("key.thecrowns.annihilate"))
                .withStyle(ChatFormatting.YELLOW));

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.thecrowns.passive.title")
                .withStyle(ChatFormatting.AQUA));
        appendSharedStats(tooltip);
        for (int i = 1; i <= 7; i++) {
            tooltip.add(Component.translatable("tooltip.thecrowns.unleashed.passive." + i)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    protected boolean isPackConfigurable() { return true; }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "thecrowns:textures/models/armor/unleashed_layer_1.png";
    }
}
