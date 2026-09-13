package com.glitchedcrown.item;

import net.minecraft.ChatFormatting;
import com.glitchedcrown.client.ClientTooltipContext;
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
        return "tooltip.glitchedcrown.unleashed";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        if (!ClientTooltipContext.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.glitchedcrown.hold_shift")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.glitchedcrown.unleashed.active.title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        for (int i = 1; i <= 4; i++) {
            tooltip.add(Component.translatable("tooltip.glitchedcrown.unleashed.active." + i)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.glitchedcrown.unleashed.keys",
                        Component.keybind("key.glitchedcrown.fire_laser"),
                        Component.keybind("key.glitchedcrown.toggle_fate"),
                        Component.keybind("key.glitchedcrown.toggle_aura"),
                        Component.keybind("key.glitchedcrown.annihilate"))
                .withStyle(ChatFormatting.YELLOW));

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.glitchedcrown.passive.title")
                .withStyle(ChatFormatting.AQUA));
        appendSharedStats(tooltip);
        for (int i = 1; i <= 7; i++) {
            tooltip.add(Component.translatable("tooltip.glitchedcrown.unleashed.passive." + i)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    protected boolean isPackConfigurable() { return true; }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "glitchedcrown:textures/models/armor/unleashed_layer_1.png";
    }
}
