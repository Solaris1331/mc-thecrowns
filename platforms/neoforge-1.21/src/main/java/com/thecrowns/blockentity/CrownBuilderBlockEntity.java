package com.thecrowns.blockentity;

import com.thecrowns.block.CrownBuilderBlock;
import com.thecrowns.item.CrownBuilderBlockItem;
import com.thecrowns.menu.CrownBuilderMenu;
import com.thecrowns.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Persistent storage for a Crown Builder.
 *
 * Deliberately does NOT implement Container and exposes no item capability. Vanilla hoppers locate
 * block entities through the Container interface, and Forge pipes look for item capabilities; keeping
 * the storage private makes the Builder player-GUI-only by construction.
 */
public final class CrownBuilderBlockEntity extends BlockEntity implements MenuProvider {
    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private final Container menuView = new MenuContainerView();

    public CrownBuilderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CROWN_BUILDER.get(), pos, state);
    }

    public int getTier() {
        return getBlockState().getBlock() instanceof CrownBuilderBlock block ? block.getTier() : 1;
    }

    public Container menuContainer() {
        return menuView;
    }

    public boolean isCompletelyEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    public void writeToItemStack(ItemStack stack) {
        CompoundTag tag = saveWithoutMetadata();
        if (!tag.isEmpty()) stack.getOrCreateTag().put("BlockEntityTag", tag);
    }

    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CrownBuilderMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        net.minecraft.world.ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        net.minecraft.world.ContainerHelper.loadAllItems(tag, items);
    }

    private final class MenuContainerView implements Container {
        @Override public int getContainerSize() { return items.size(); }
        @Override public boolean isEmpty() { return isCompletelyEmpty(); }
        @Override public ItemStack getItem(int slot) { return items.get(slot); }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack result = net.minecraft.world.ContainerHelper.removeItem(items, slot, amount);
            if (!result.isEmpty()) CrownBuilderBlockEntity.this.setChanged();
            return result;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return net.minecraft.world.ContainerHelper.takeItem(items, slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            items.set(slot, stack);
            if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
            CrownBuilderBlockEntity.this.setChanged();
        }

        @Override
        public void setChanged() { CrownBuilderBlockEntity.this.setChanged(); }

        @Override
        public boolean stillValid(Player player) {
            return CrownBuilderBlockEntity.this.stillValid(player);
        }

        @Override
        public void clearContent() {
            items.clear();
            CrownBuilderBlockEntity.this.setChanged();
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return CrownBuilderBlockItem.tierOf(stack) <= 0;
        }
    }
}
