package com.glitchedcrown.tags;

import com.glitchedcrown.GlitchedCrownMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/** Datapack extension points for pack authors. */
public final class CrownTags {
    public static final TagKey<EntityType<?>> PROTECTED_FROM_CROWN_OFFENSE = tag("protected_from_crown_offense");
    public static final TagKey<EntityType<?>> PROTECTED_FROM_REMOVAL_RAY = tag("protected_from_removal_ray");
    public static final TagKey<EntityType<?>> PROTECTED_FROM_ANNIHILATION = tag("protected_from_annihilation");
    public static final TagKey<EntityType<?>> PROTECTED_FROM_EXECUTION = tag("protected_from_execution");

    private static TagKey<EntityType<?>> tag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(GlitchedCrownMod.MOD_ID, path));
    }
    private CrownTags() {}
}
