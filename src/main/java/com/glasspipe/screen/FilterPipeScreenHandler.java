package com.glasspipe.screen;

import com.glasspipe.block.entity.FilterPipeBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

/**
 * Screen handler for the filter pipe GUI.
 * Shows 9 filter slots and a mode toggle button (whitelist/blacklist).
 */
public class FilterPipeScreenHandler extends ScreenHandler {

    private final Inventory filterInventory;
    private final FilterPipeBlockEntity blockEntity;

    /** Server-side constructor (opened from block entity) */
    public FilterPipeScreenHandler(int syncId, PlayerInventory playerInventory,
                                    FilterPipeBlockEntity blockEntity) {
        super(ModScreenHandlers.FILTER_PIPE_SCREEN_HANDLER, syncId);
        this.filterInventory = blockEntity.getFilterInventory();
        this.blockEntity = blockEntity;

        checkSize(filterInventory, 9);
        filterInventory.onOpen(playerInventory.player);

        // Filter slots (3x3 grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(filterInventory, row * 3 + col,
                        44 + col * 18, 17 + row * 18));
            }
        }

        // Player inventory (3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    /** Client-side constructor (opened from packet with BlockPos) */
    public FilterPipeScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(ModScreenHandlers.FILTER_PIPE_SCREEN_HANDLER, syncId);
        this.filterInventory = new SimpleInventory(9);
        this.blockEntity = null;

        // Filter slots (3x3 grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(filterInventory, row * 3 + col,
                        44 + col * 18, 17 + row * 18));
            }
        }

        // Player inventory (3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);

        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (slotIndex < 9) {
                // From filter slots to player inventory
                if (!insertItem(originalStack, 9, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory to filter slots
                if (!insertItem(originalStack, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return filterInventory.canPlayerUse(player);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        filterInventory.onClose(player);
    }

    public FilterPipeBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean isWhitelistMode() {
        return blockEntity != null && blockEntity.isWhitelistMode();
    }
}
