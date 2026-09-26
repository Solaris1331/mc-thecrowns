package com.thecrowns.registry;

import com.thecrowns.TheCrownsMod;
import com.thecrowns.menu.CrownBuilderMenu;
import com.thecrowns.menu.CrownLorebookMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.function.Supplier;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, TheCrownsMod.MOD_ID);

    public static final Supplier<MenuType<CrownLorebookMenu>> CROWN_LOREBOOK =
            MENUS.register("crown_lorebook", () -> IMenuTypeExtension.create(CrownLorebookMenu::new));

    public static final Supplier<MenuType<CrownBuilderMenu>> CROWN_BUILDER =
            MENUS.register("crown_builder", () -> IMenuTypeExtension.create((windowId, inv, buf) ->
                    new CrownBuilderMenu(windowId, inv, buf.readBlockPos())));

    private ModMenus() {}
}
