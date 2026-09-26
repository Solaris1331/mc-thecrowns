package com.thecrowns.util;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Safe access to legacy mod state stored in the 1.21 custom-data component. */
public final class ItemStackCustomData {
    private ItemStackCustomData() {
    }

    public static CompoundTag copy(ItemStack stack) {
        return stack == null || stack.isEmpty()
                ? new CompoundTag()
                : stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static boolean contains(ItemStack stack, String key) {
        return stack != null && !stack.isEmpty()
                && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains(key);
    }

    public static long getLong(ItemStack stack, String key, long fallback) {
        CompoundTag tag = copy(stack);
        return tag.contains(key) ? tag.getLong(key) : fallback;
    }

    public static int getInt(ItemStack stack, String key, int fallback) {
        CompoundTag tag = copy(stack);
        return tag.contains(key) ? tag.getInt(key) : fallback;
    }

    public static boolean getBoolean(ItemStack stack, String key, boolean fallback) {
        CompoundTag tag = copy(stack);
        return tag.contains(key) ? tag.getBoolean(key) : fallback;
    }

    public static void update(ItemStack stack, Consumer<CompoundTag> update) {
        if (stack == null || stack.isEmpty()) return;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, update);
    }

    /** Removes an inert component created by older tooltip synchronisation. */
    public static void clearIfEmpty(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null && data.copyTag().isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        }
    }

    public static void set(ItemStack stack, CompoundTag tag) {
        if (stack == null || stack.isEmpty()) return;
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }
}
