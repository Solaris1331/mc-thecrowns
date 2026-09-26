package com.thecrowns.registry;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.blockentity.CrownBuilderBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, TheCrownsMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<CrownBuilderBlockEntity>> CROWN_BUILDER =
            BLOCK_ENTITIES.register("crown_builder", () -> BlockEntityType.Builder.of(CrownBuilderBlockEntity::new,
                    ModBlocks.CROWN_BUILDER_T1.get(), ModBlocks.CROWN_BUILDER_T2.get(),
                    ModBlocks.CROWN_BUILDER_T3.get(), ModBlocks.CROWN_BUILDER_T4.get()).build(null));

    private ModBlockEntities() {}
}
