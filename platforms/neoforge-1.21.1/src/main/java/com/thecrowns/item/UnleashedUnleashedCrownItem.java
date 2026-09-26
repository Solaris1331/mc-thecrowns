package com.thecrowns.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

/**
 * Immutable creative/admin reference Crown. It always receives the complete
 * Unleashed ruleset and intentionally ignores every server config switch.
 */
public final class UnleashedUnleashedCrownItem extends UnleashedCrownItem {
    public UnleashedUnleashedCrownItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    protected boolean usesFixedFullPowerStats() { return true; }

    @Override
    protected boolean isPackConfigurable() { return false; }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public net.minecraft.resources.ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                                                     ArmorMaterial.Layer layer, boolean innerModel) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("thecrowns", "textures/models/armor/unleashed_unleashed_layer_1.png");
    }
}
