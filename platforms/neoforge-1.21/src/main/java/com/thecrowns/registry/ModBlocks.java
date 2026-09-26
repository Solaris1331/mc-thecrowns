package com.thecrowns.registry;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.block.CrownBuilderBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, TheCrownsMod.MOD_ID);

    public static final RegistryObject<Block> ROTTEN_FLESH_BLOCK = BLOCKS.register("rotten_flesh_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.HAY_BLOCK)
                    .strength(0.8F)
                    .sound(SoundType.WART_BLOCK)));

    public static final RegistryObject<Block> BIG_ROTTEN_FLESH_BLOCK = BLOCKS.register("big_rotten_flesh_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.HAY_BLOCK)
                    .strength(1.4F)
                    .sound(SoundType.WART_BLOCK)));


    public static final RegistryObject<Block> COMPRESSED_EMERALD_BLOCK = BLOCKS.register("compressed_emerald_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.EMERALD_BLOCK)
                    .strength(7.0F, 1200.0F)
                    .sound(SoundType.AMETHYST)));

    private static final float BUILDER_BLAST_RESISTANCE = 3_600_000.0F;

    public static final RegistryObject<Block> CROWN_BUILDER_T1 = BLOCKS.register("crown_builder_t1",
            () -> new CrownBuilderBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(5.0F, BUILDER_BLAST_RESISTANCE).sound(SoundType.ANVIL).requiresCorrectToolForDrops(), 1));

    public static final RegistryObject<Block> CROWN_BUILDER_T2 = BLOCKS.register("crown_builder_t2",
            () -> new CrownBuilderBlock(BlockBehaviour.Properties.copy(Blocks.DIAMOND_BLOCK)
                    .strength(5.0F, BUILDER_BLAST_RESISTANCE).sound(SoundType.METAL).requiresCorrectToolForDrops(), 2));

    public static final RegistryObject<Block> CROWN_BUILDER_T3 = BLOCKS.register("crown_builder_t3",
            () -> new CrownBuilderBlock(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
                    .strength(50.0F, BUILDER_BLAST_RESISTANCE).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops(), 3));

    public static final RegistryObject<Block> CROWN_BUILDER_T4 = BLOCKS.register("crown_builder_t4",
            () -> new CrownBuilderBlock(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                    .strength(50.0F, BUILDER_BLAST_RESISTANCE).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops(), 4));

    private ModBlocks() {}
}
