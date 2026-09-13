package com.glitchedcrown.registry;

import com.glitchedcrown.GlitchedCrownMod;
import com.glitchedcrown.blockentity.CrownBuilderBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GlitchedCrownMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<CrownBuilderBlockEntity>> CROWN_BUILDER =
            BLOCK_ENTITIES.register("crown_builder", () -> BlockEntityType.Builder.of(CrownBuilderBlockEntity::new,
                    ModBlocks.CROWN_BUILDER_T1.get(), ModBlocks.CROWN_BUILDER_T2.get(),
                    ModBlocks.CROWN_BUILDER_T3.get(), ModBlocks.CROWN_BUILDER_T4.get()).build(null));

    private ModBlockEntities() {}
}
