package com.thecrowns.util;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adapts the pre-1.20.5 potion helpers to the 1.21 data-component representation. */
public final class PotionUtils {
    private PotionUtils() {}

    public static Holder<Potion> getPotion(ItemStack stack) {
        return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                .potion().orElse(Potions.WATER);
    }

    public static boolean hasNoPotion(ItemStack stack) {
        return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().isEmpty();
    }

    public static List<MobEffectInstance> getMobEffects(ItemStack stack) {
        List<MobEffectInstance> effects = new ArrayList<>();
        stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects().forEach(effects::add);
        return effects;
    }

    public static void setPotion(ItemStack stack, Holder<Potion> potion) {
        PotionContents current = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        stack.set(DataComponents.POTION_CONTENTS, current.withPotion(potion));
    }

    public static void setCustomEffects(ItemStack stack, List<MobEffectInstance> effects) {
        stack.set(DataComponents.POTION_CONTENTS,
                new PotionContents(Optional.empty(), Optional.empty(), List.copyOf(effects)));
    }
}
