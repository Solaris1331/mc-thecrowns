package com.thecrowns.logic;

import com.thecrowns.TheCrownsMod;
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
            "cursed_crown");

    private GlitchedCrownFusion() {}

    public static ResourceLocation getFusion(ItemStack stack) {
        if (stack.isEmpty() || stack.getTag() == null) return null;
        if (stack.getTag().contains(TAG_FUSED_CROWN_STACK, Tag.TAG_COMPOUND)) {
            ItemStack fusedStack = ItemStack.of(stack.getTag().getCompound(TAG_FUSED_CROWN_STACK));
            ResourceLocation storedId = BuiltInRegistries.ITEM.getKey(fusedStack.getItem());
            ResourceLocation valid = validItemId(storedId.toString());
            if (valid != null) return valid;
        }
        if (stack.getTag().contains(TAG_FUSED_CROWN, Tag.TAG_STRING)) {
            return validItemId(stack.getTag().getString(TAG_FUSED_CROWN));
        }
        // Source compatibility for the earlier, unbuilt multi-fusion foundation:
        // the most recently appended entry becomes the sole active ability.
        ListTag legacy = stack.getTag().getList(LEGACY_TAG_FUSED_CROWNS, Tag.TAG_STRING);
        return legacy.isEmpty() ? null : validItemId(legacy.getString(legacy.size() - 1));
    }

    public static boolean hasFusion(ItemStack stack, ResourceLocation crownId) {
        ResourceLocation current = getFusion(stack);
        return crownId != null && crownId.equals(current);
    }

    public static ItemStack getFusionStack(ItemStack stack) {
        if (stack.isEmpty() || stack.getTag() == null) return ItemStack.EMPTY;
        if (stack.getTag().contains(TAG_FUSED_CROWN_STACK, Tag.TAG_COMPOUND)) {
            ItemStack stored = ItemStack.of(stack.getTag().getCompound(TAG_FUSED_CROWN_STACK));
            if (getFusion(stack) != null && !stored.isEmpty()) {
                stored.setCount(1);
                return stored;
            }
        }
        ResourceLocation id = getFusion(stack);
        return id == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(id));
    }

    public static void setFusion(ItemStack stack, ItemStack crownStack) {
        if (stack.isEmpty() || crownStack.isEmpty()) return;
        ResourceLocation crownId = validItemId(BuiltInRegistries.ITEM.getKey(crownStack.getItem()).toString());
        if (crownId == null) return;
        ItemStack stored = crownStack.copy();
        stored.setCount(1);
        stack.getOrCreateTag().putString(TAG_FUSED_CROWN, crownId.toString());
        stack.getOrCreateTag().put(TAG_FUSED_CROWN_STACK, stored.save(new CompoundTag()));
        stack.getOrCreateTag().remove(LEGACY_TAG_FUSED_CROWNS);
    }

    public static void markPendingReturn(ItemStack stack, ItemStack crownStack) {
        if (!stack.isEmpty() && !crownStack.isEmpty()) {
            ItemStack returned = crownStack.copy();
            returned.setCount(1);
            ResourceLocation crownId = BuiltInRegistries.ITEM.getKey(returned.getItem());
            stack.getOrCreateTag().putString(TAG_PENDING_RETURN, crownId.toString());
            stack.getOrCreateTag().put(TAG_PENDING_RETURN_STACK, returned.save(new CompoundTag()));
        }
    }

    public static ItemStack takePendingReturn(ItemStack stack) {
        if (stack.isEmpty() || stack.getTag() == null) return ItemStack.EMPTY;
        if (stack.getTag().contains(TAG_PENDING_RETURN_STACK, Tag.TAG_COMPOUND)) {
            ItemStack returned = ItemStack.of(stack.getTag().getCompound(TAG_PENDING_RETURN_STACK));
            stack.getTag().remove(TAG_PENDING_RETURN_STACK);
            stack.getTag().remove(TAG_PENDING_RETURN);
            if (!returned.isEmpty() && validItemId(BuiltInRegistries.ITEM.getKey(returned.getItem()).toString()) != null) {
                returned.setCount(1);
                return returned;
            }
            return ItemStack.EMPTY;
        }
        if (!stack.getTag().contains(TAG_PENDING_RETURN, Tag.TAG_STRING)) return ItemStack.EMPTY;
        ResourceLocation id = validItemId(stack.getTag().getString(TAG_PENDING_RETURN));
        stack.getTag().remove(TAG_PENDING_RETURN);
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
