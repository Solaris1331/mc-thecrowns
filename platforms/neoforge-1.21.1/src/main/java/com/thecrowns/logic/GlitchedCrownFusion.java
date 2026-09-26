package com.thecrowns.logic;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.util.ItemStackCustomData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/** Persistent single-slot Crown ability fused into a Glitched Crown. */
public final class GlitchedCrownFusion {
    public static final String TAG_FUSED_CROWN = "thecrowns_fused_crown";
    public static final String TAG_FUSED_CROWN_STACK = "thecrowns_fused_crown_stack";
    public static final String TAG_PENDING_RETURN = "thecrowns_pending_fusion_return";
    public static final String TAG_PENDING_RETURN_STACK = "thecrowns_pending_fusion_return_stack";
    private static final String LEGACY_TAG_FUSED_CROWNS = "thecrowns_fused_crowns";
    private static final Set<String> SUPPORTED_PATHS = Set.of(
            "burning_crown", "ironforged_crown", "frost_crown",
            "bloody_crown", "darkened_crown", "warrior_crown", "divine_crown",
            "crown_of_light", "dimensional_crown", "angelic_crown", "temporal_crown",
            "cursed_crown", "shadow_crown", "abyssal_crown");

    private GlitchedCrownFusion() {}

    public static ResourceLocation getFusion(ItemStack stack) {
        if (stack.isEmpty()) return null;
        CompoundTag data = ItemStackCustomData.copy(stack);
        if (data.contains(TAG_FUSED_CROWN, Tag.TAG_STRING)) {
            return validItemId(data.getString(TAG_FUSED_CROWN));
        }
        // Source compatibility for the earlier, unbuilt multi-fusion foundation:
        // the most recently appended entry becomes the sole active ability.
        ListTag legacy = data.getList(LEGACY_TAG_FUSED_CROWNS, Tag.TAG_STRING);
        return legacy.isEmpty() ? null : validItemId(legacy.getString(legacy.size() - 1));
    }

    public static boolean hasFusion(ItemStack stack, ResourceLocation crownId) {
        ResourceLocation current = getFusion(stack);
        return crownId != null && crownId.equals(current);
    }

    public static ItemStack getFusionStack(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ResourceLocation id = getFusion(stack);
        return id == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(id));
    }

    public static void setFusion(ItemStack stack, ItemStack crownStack) {
        if (stack.isEmpty() || crownStack.isEmpty()) return;
        ResourceLocation crownId = validItemId(BuiltInRegistries.ITEM.getKey(crownStack.getItem()).toString());
        if (crownId == null) return;
        ItemStackCustomData.update(stack, data -> {
            data.putString(TAG_FUSED_CROWN, crownId.toString());
            data.remove(TAG_FUSED_CROWN_STACK);
            data.remove(LEGACY_TAG_FUSED_CROWNS);
        });
    }

    public static void markPendingReturn(ItemStack stack, ItemStack crownStack) {
        if (!stack.isEmpty() && !crownStack.isEmpty()) {
            ResourceLocation crownId = BuiltInRegistries.ITEM.getKey(crownStack.getItem());
            ItemStackCustomData.update(stack, data -> {
                data.putString(TAG_PENDING_RETURN, crownId.toString());
                data.remove(TAG_PENDING_RETURN_STACK);
            });
        }
    }

    public static ItemStack takePendingReturn(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        CompoundTag data = ItemStackCustomData.copy(stack);
        if (!data.contains(TAG_PENDING_RETURN, Tag.TAG_STRING)) return ItemStack.EMPTY;
        ResourceLocation id = validItemId(data.getString(TAG_PENDING_RETURN));
        ItemStackCustomData.update(stack, tags -> {
            tags.remove(TAG_PENDING_RETURN);
            tags.remove(TAG_PENDING_RETURN_STACK);
        });
        return id == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(id));
    }

    public static boolean hasActiveFusion(LivingEntity wearer, Item crown) {
        return matches(getActiveFusion(wearer), crown);
    }

    public static ResourceLocation getActiveFusion(LivingEntity wearer) {
        if (wearer == null || !CrownLogic.isNerfedCrownActive(wearer)) return null;
        return getFusion(CrownLogic.getWornGlitchedCrown(wearer));
    }

    public static boolean matches(ResourceLocation fusionId, Item crown) {
        return fusionId != null && crown != null && fusionId.equals(BuiltInRegistries.ITEM.getKey(crown));
    }

    private static ResourceLocation validItemId(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        return id != null && TheCrownsMod.MOD_ID.equals(id.getNamespace())
                && SUPPORTED_PATHS.contains(id.getPath()) && BuiltInRegistries.ITEM.containsKey(id) ? id : null;
    }
}
