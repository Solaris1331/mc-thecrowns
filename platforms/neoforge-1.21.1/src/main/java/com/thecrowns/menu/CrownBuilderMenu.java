package com.thecrowns.menu;

import com.thecrowns.blockentity.CrownBuilderBlockEntity;
import com.thecrowns.item.CrownBuilderBlockItem;
import com.thecrowns.logic.CrownAdvancements;
import com.thecrowns.recipe.CrownBuilderRecipe;
import com.thecrowns.recipe.CrownBuilderRecipeInput;
import com.thecrowns.registry.ModMenus;
import com.thecrowns.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public final class CrownBuilderMenu extends AbstractContainerMenu {
    public static final int RESULT_SLOT = 0;
    public static final int INPUT_SLOT_START = 1;
    public static final int INPUT_SLOT_COUNT = 9;
    public static final int PLAYER_SLOT_START = 10;
    public static final int PLAYER_SLOT_COUNT = 36;

    private final CrownBuilderBlockEntity blockEntity;
    private final Container input;
    private final ResultContainer result = new ResultContainer();
    private final Level level;
    private final Player owner;
    private final int builderTier;
    private CrownBuilderRecipe currentRecipe;

    public CrownBuilderMenu(int containerId, Inventory playerInventory, CrownBuilderBlockEntity blockEntity) {
        super(ModMenus.CROWN_BUILDER.get(), containerId);
        this.blockEntity = blockEntity;
        this.input = blockEntity.menuContainer();
        this.level = playerInventory.player.level();
        this.owner = playerInventory.player;
        this.builderTier = blockEntity.getTier();

        addSlot(new ResultSlot(0, 124, 43));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = col + row * 3;
                addSlot(new InputSlot(input, index, 30 + col * 18, 25 + row * 18));
            }
        }
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        if (!level.isClientSide) updateResult(owner);
    }

    public CrownBuilderMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, resolve(playerInventory.player.level(), pos));
    }

    private static CrownBuilderBlockEntity resolve(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CrownBuilderBlockEntity builder)) {
            throw new IllegalStateException("Crown Builder block entity missing at " + pos);
        }
        return builder;
    }

    public int getBuilderTier() { return builderTier; }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (!level.isClientSide) updateResult(owner);
    }

    private void updateResult(Player player) {
        if (level.isClientSide) return;
        CrownBuilderRecipeInput recipeInput = CrownBuilderRecipeInput.of(input);
        Optional<CrownBuilderRecipe> match = level.getRecipeManager()
                .getRecipeFor(ModRecipes.CROWN_BUILDER_TYPE.get(), recipeInput, level)
                .map(net.minecraft.world.item.crafting.RecipeHolder::value);
        currentRecipe = null;
        ItemStack output = ItemStack.EMPTY;
        if (match.isPresent()) {
            CrownBuilderRecipe recipe = match.get();
            boolean allowed = player instanceof ServerPlayer serverPlayer
                    && recipe.canPlayerCraft(serverPlayer, builderTier);
            if (allowed && canAcceptRemainders(recipe.getRemainingItems(recipeInput))) {
                currentRecipe = recipe;
                output = recipe.assemble(recipeInput, level.registryAccess());
            }
        }
        result.setItem(0, output);
        broadcastChanges();
    }

    private boolean canAcceptRemainders(NonNullList<ItemStack> remainders) {
        for (int i = 0; i < 9; i++) {
            ItemStack remainder = remainders.get(i);
            if (remainder.isEmpty()) continue;
            ItemStack existing = input.getItem(i);
            int existingAfterConsume = existing.isEmpty() ? 0 : existing.getCount() - 1;
            if (existingAfterConsume <= 0) continue;
            if (!ItemStack.isSameItemSameComponents(existing, remainder)) return false;
            if (existingAfterConsume + remainder.getCount() > existing.getMaxStackSize()) return false;
        }
        return true;
    }

    private boolean consumeIngredients(Player player, CrownBuilderRecipe recipe) {
        NonNullList<ItemStack> remainders = recipe.getRemainingItems(CrownBuilderRecipeInput.of(input));
        if (!canAcceptRemainders(remainders)) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack existing = input.getItem(i);
            ItemStack remainder = remainders.get(i);
            if (!existing.isEmpty()) input.removeItem(i, 1);
            ItemStack post = input.getItem(i);
            if (!remainder.isEmpty()) {
                if (post.isEmpty()) input.setItem(i, remainder.copy());
                else if (ItemStack.isSameItemSameComponents(post, remainder)) {
                    post.grow(remainder.getCount());
                    input.setItem(i, post);
                } else {
                    return false; // defensive; pickup was already refused by canAcceptRemainders
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index == RESULT_SLOT) {
            slot.onQuickCraft(stack, original);
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_START + PLAYER_SLOT_COUNT, true)) return ItemStack.EMPTY;
            slot.onTake(player, stack);
            return original;
        }

        if (index >= INPUT_SLOT_START && index < INPUT_SLOT_START + INPUT_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_START + PLAYER_SLOT_COUNT, false)) return ItemStack.EMPTY;
        } else {
            if (CrownBuilderBlockItem.tierOf(stack) > 0) return ItemStack.EMPTY;
            if (!moveItemStackTo(stack, INPUT_SLOT_START, INPUT_SLOT_START + INPUT_SLOT_COUNT, false)) {
                if (index < PLAYER_SLOT_START + 27) {
                    if (!moveItemStackTo(stack, PLAYER_SLOT_START + 27, PLAYER_SLOT_START + 36, false)) return ItemStack.EMPTY;
                } else if (!moveItemStackTo(stack, PLAYER_SLOT_START, PLAYER_SLOT_START + 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    @Override public boolean stillValid(Player player) { return blockEntity.stillValid(player); }

    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 98 + row * 18));
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int col = 0; col < 9; col++) addSlot(new Slot(inv, col, 8 + col * 18, 156));
    }

    private final class InputSlot extends Slot {
        private InputSlot(Container container, int index, int x, int y) { super(container, index, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return CrownBuilderBlockItem.tierOf(stack) <= 0 && super.mayPlace(stack); }
    }

    private final class ResultSlot extends Slot {
        private ResultSlot(int index, int x, int y) { super(result, index, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }

        @Override
        public boolean mayPickup(Player player) {
            updateResult(player);
            return currentRecipe != null && hasItem()
                    && canAcceptRemainders(currentRecipe.getRemainingItems(CrownBuilderRecipeInput.of(input)));
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            CrownBuilderRecipe recipe = currentRecipe;
            ItemStack craftedResult = recipe == null ? ItemStack.EMPTY
                    : recipe.assemble(CrownBuilderRecipeInput.of(input), level.registryAccess());
            if (recipe != null && consumeIngredients(player, recipe) && player instanceof ServerPlayer serverPlayer) {
                // Use the recipe result instead of the Slot stack because shift-click may have already
                // moved the slot stack into the player's inventory and reduced it to EMPTY.
                CrownAdvancements.awardCrafted(serverPlayer, craftedResult);
            }
            updateResult(player);
            super.onTake(player, stack);
        }
    }
}
