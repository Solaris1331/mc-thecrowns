package com.glitchedcrown.registry;

import com.glitchedcrown.GlitchedCrownMod;
import com.glitchedcrown.menu.CrownBuilderMenu;
import com.glitchedcrown.menu.CrownLorebookMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, GlitchedCrownMod.MOD_ID);

    public static final RegistryObject<MenuType<CrownLorebookMenu>> CROWN_LOREBOOK =
            MENUS.register("crown_lorebook", () -> IForgeMenuType.create(CrownLorebookMenu::new));

    public static final RegistryObject<MenuType<CrownBuilderMenu>> CROWN_BUILDER =
            MENUS.register("crown_builder", () -> IForgeMenuType.create((windowId, inv, buf) ->
                    new CrownBuilderMenu(windowId, inv, buf.readBlockPos())));

    private ModMenus() {}
}
