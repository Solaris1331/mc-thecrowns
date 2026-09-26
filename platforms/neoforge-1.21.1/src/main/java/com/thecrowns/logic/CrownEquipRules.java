package com.thecrowns.logic;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/** Equipment rule shared by vanilla armor slots and Curios: a specific Crown type may only be worn once. */
public final class CrownEquipRules {
    private CrownEquipRules() {}

    public static boolean canEquipUnique(LivingEntity wearer, ItemStack candidate) {
        if (wearer == null || candidate == null || candidate.isEmpty()) return true;
        Item item = candidate.getItem();

        ItemStack head = wearer.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.isEmpty() && head.is(item)) return false;

        try {
            return CuriosApi.getCuriosInventory(wearer).map(handler -> {
                for (var curioHandler : handler.getCurios().values()) {
                    var stacks = curioHandler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack equipped = stacks.getStackInSlot(i);
                        if (!equipped.isEmpty() && equipped.is(item)) return false;
                    }
                }
                return true;
            }).orElse(true);
        } catch (RuntimeException ignored) {
            return true;
        }
    }
}
