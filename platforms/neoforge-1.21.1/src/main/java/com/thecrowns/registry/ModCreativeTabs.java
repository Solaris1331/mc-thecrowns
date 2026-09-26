package com.thecrowns.registry;

import com.thecrowns.TheCrownsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

/** The Crowns' single dedicated creative inventory tab. */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheCrownsMod.MOD_ID);

    public static final Supplier<CreativeModeTab> THE_CROWNS = TABS.register("the_crowns", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.thecrowns.the_crowns"))
                    .icon(() -> ModItems.GLITCHED_CROWN.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        // Keep one authoritative location for every player-facing item registered by this mod.
                        // Administrative items such as U^2 stay command-only and are intentionally hidden.
                        ModItems.ITEMS.getEntries().forEach(entry -> {
                            if (entry.get() == ModItems.UNLEASHED_UNLEASHED_CROWN.get()) return;
                            output.accept(entry.get());
                        });
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
