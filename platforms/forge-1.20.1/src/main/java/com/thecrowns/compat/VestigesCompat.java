package com.thecrowns.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/**
 * Soft compatibility with Vestiges of the Present. No VP class is linked, so
 * the Crown mod remains loadable when VP is absent.
 */
public final class VestigesCompat {
    private VestigesCompat() {}

    /** Clears the known VP death-stage shield guards after a raw-health lethal hit. */
    public static void clearDeathGuards(LivingEntity target) {
        if (target == null) return;
        CompoundTag tag = target.getPersistentData();
        if (tag.contains("VPOverShield")) tag.putFloat("VPOverShield", 0.0F);
        if (tag.contains("VPOverShieldMax")) tag.putFloat("VPOverShieldMax", 0.0F);
        if (tag.contains("VPShield")) tag.putFloat("VPShield", 0.0F);
        if (tag.contains("VPShieldInit")) tag.putFloat("VPShieldInit", 0.0F);
    }
}
