package com.glitchedcrown.item;

import com.glitchedcrown.menu.CrownLorebookMenu;
import com.glitchedcrown.logic.CrownAdvancementGate;
import com.glitchedcrown.logic.CrownAdvancements;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/** In-game reference manual for The Crowns. */
public final class CrownLorebookItem extends Item {
    public CrownLorebookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            boolean unleashedUnlocked = CrownAdvancementGate.hasGlitchedCrownAdvancement(serverPlayer);
            boolean cursedUnlocked = CrownAdvancements.done(serverPlayer, "cursed_crown_acquired");
            var coreStatus = CrownAdvancementGate.coreStatus(serverPlayer);
            var substituteStatus = CrownAdvancementGate.substituteStatus(serverPlayer);
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (containerId, inventory, ignored) -> new CrownLorebookMenu(
                            containerId, inventory, unleashedUnlocked, cursedUnlocked, coreStatus, substituteStatus),
                    Component.translatable("gui.glitchedcrown.lorebook.title")
            ), buf -> CrownLorebookMenu.writeOpenData(buf, unleashedUnlocked, cursedUnlocked, coreStatus, substituteStatus));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
