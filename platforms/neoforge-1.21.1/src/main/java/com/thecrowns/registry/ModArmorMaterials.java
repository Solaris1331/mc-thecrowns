package com.thecrowns.registry;

import com.thecrowns.TheCrownsMod;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

/** 1.21 represents armor materials as data-backed records rather than an interface implementation. */
public final class ModArmorMaterials {
    public static final Holder<ArmorMaterial> GLITCHED = Holder.direct(new ArmorMaterial(
            defenses(),
            1,
            SoundEvents.ARMOR_EQUIP_GENERIC,
            () -> Ingredient.EMPTY,
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(TheCrownsMod.MOD_ID, "glitched"))),
            0.0F,
            0.0F
    ));

    private ModArmorMaterials() {}

    private static EnumMap<ArmorItem.Type, Integer> defenses() {
        EnumMap<ArmorItem.Type, Integer> values = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values()) values.put(type, 0);
        return values;
    }
}
