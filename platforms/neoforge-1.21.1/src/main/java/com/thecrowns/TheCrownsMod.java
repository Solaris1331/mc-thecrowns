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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

@Mod(TheCrownsMod.MOD_ID)
public final class TheCrownsMod {
    public static final String MOD_ID = "thecrowns";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TheCrownsMod(IEventBus modBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModRecipes.TYPES.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);
        modBus.addListener(CrownCurios::enqueueImc);
        modBus.addListener(ModNetworking::register);
        modContainer.registerConfig(ModConfig.Type.SERVER, CrownServerConfig.SPEC, "thecrowns-server.toml");
    }
}
