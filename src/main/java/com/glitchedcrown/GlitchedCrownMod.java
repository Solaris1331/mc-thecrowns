package com.glitchedcrown;

import com.glitchedcrown.network.ModNetworking;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import com.glitchedcrown.config.CrownServerConfig;
import com.glitchedcrown.registry.ModItems;
import com.glitchedcrown.registry.ModCreativeTabs;
import com.glitchedcrown.registry.ModBlocks;
import com.glitchedcrown.registry.ModRecipes;
import com.glitchedcrown.registry.ModMenus;
import com.glitchedcrown.registry.ModBlockEntities;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@Mod(GlitchedCrownMod.MOD_ID)
public final class GlitchedCrownMod {
    public static final String MOD_ID = "glitchedcrown";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GlitchedCrownMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModRecipes.TYPES.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);
        ModNetworking.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CrownServerConfig.SPEC, "glitchedcrown-server.toml");
    }
}
