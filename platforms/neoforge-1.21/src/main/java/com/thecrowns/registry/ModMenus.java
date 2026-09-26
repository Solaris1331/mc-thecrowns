package com.thecrowns.registry;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.menu.CrownBuilderMenu;
import com.thecrowns.menu.CrownLorebookMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IForgeMenuType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, TheCrownsMod.MOD_ID);

    public static final RegistryObject<MenuType<CrownLorebookMenu>> CROWN_LOREBOOK =
            MENUS.register("crown_lorebook", () -> IForgeMenuType.create(CrownLorebookMenu::new));

    public static final RegistryObject<MenuType<CrownBuilderMenu>> CROWN_BUILDER =
            MENUS.register("crown_builder", () -> IForgeMenuType.create((windowId, inv, buf) ->
                    new CrownBuilderMenu(windowId, inv, buf.readBlockPos())));

    private ModMenus() {}
}
