package com.thecrowns.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;

/** Registry-id-only Ice and Fire compatibility; no hard dependency. */
public final class IceAndFireCompat {
    private IceAndFireCompat() {}

    /** Ice and Fire dragons leave a harvestable corpse entity after normal death. */
    public static boolean preservesDragonCorpse(LivingEntity target) {
        if (target == null) return false;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (id == null || !"iceandfire".equals(id.getNamespace())) return false;
        return switch (id.getPath()) {
            case "fire_dragon", "ice_dragon", "lightning_dragon" -> true;
            default -> false;
        };
    }
}
