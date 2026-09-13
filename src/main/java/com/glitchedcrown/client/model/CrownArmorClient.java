package com.glitchedcrown.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Client armor hooks shared by every visible crown item. */
public final class CrownArmorClient {
    private CrownArmorClient() {}

    public static void register(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                          EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (equipmentSlot != EquipmentSlot.HEAD) return original;
                CrownArmorModel<LivingEntity> model = CrownArmorModel.get();
                @SuppressWarnings("unchecked")
                HumanoidModel<LivingEntity> source = (HumanoidModel<LivingEntity>) original;
                source.copyPropertiesTo(model);
                model.setAllVisible(false);
                model.head.visible = true;
                model.hat.visible = false;
                return model;
            }
        });
    }
}
