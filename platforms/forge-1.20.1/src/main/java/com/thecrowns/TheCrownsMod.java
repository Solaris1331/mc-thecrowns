package com.thecrowns;

import com.thecrowns.compat.CrownCurios;
import com.thecrowns.network.ModNetworking;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import com.thecrowns.config.CrownServerConfig;
import com.thecrowns.registry.ModItems;
import com.thecrowns.registry.ModCreativeTabs;
import com.thecrowns.registry.ModBlocks;
import com.thecrowns.registry.ModRecipes;
import com.thecrowns.registry.ModMenus;
import com.thecrowns.registry.ModBlockEntities;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@Mod(TheCrownsMod.MOD_ID)
public final class TheCrownsMod {
    public static final String MOD_ID = "thecrowns";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheCrownsMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModRecipes.TYPES.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);
        modBus.addListener(CrownCurios::enqueueImc);
        ModNetworking.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CrownServerConfig.SPEC, "thecrowns-server.toml");
    }
}
