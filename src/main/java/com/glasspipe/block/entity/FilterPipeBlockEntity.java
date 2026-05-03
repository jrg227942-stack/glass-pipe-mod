package com.glasspipe.block.entity;

import com.glasspipe.screen.FilterPipeScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Filter pipe block entity. Extends the glass pipe with a 9-slot filter inventory
 * and whitelist/blacklist mode toggle.
 */
public class FilterPipeBlockEntity extends GlassPipeBlockEntity implements NamedScreenHandlerFactory {

    /** Filter inventory: 9 slots for filter items */
    private final SimpleInventory filterInventory = new SimpleInventory(9);

    /** true = whitelist (only listed items pass), false = blacklist (listed items blocked) */
    private boolean whitelistMode = true;

    public FilterPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FILTER_PIPE, pos, state);
    }

    // =========================================================================
    // TICK (delegates to parent, which calls tryExtractFromNeighbors)
    // =========================================================================

    public static void serverTick(World world, BlockPos pos, BlockState state,
                                   FilterPipeBlockEntity pipe) {
        pipe.serverTickInternal(world, pos, state);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state,
                                   FilterPipeBlockEntity pipe) {
        GlassPipeBlockEntity.clientTick(world, pos, state, pipe);
    }

    // =========================================================================
    // FILTER LOGIC
    // =========================================================================

    /**
     * Checks whether the given item is allowed to pass through this filter pipe.
     *
     * @param stack The item to check
     * @return true if the item is allowed to pass
     */
    public boolean allowsItem(ItemStack stack) {
        boolean inFilter = isInFilter(stack);

        if (whitelistMode) {
            // Whitelist: item must be in the filter list
            // Empty filter = allow all (no restrictions configured yet)
            if (isFilterEmpty()) return true;
            return inFilter;
        } else {
            // Blacklist: item must NOT be in the filter list
            return !inFilter;
        }
    }

    private boolean isInFilter(ItemStack stack) {
        for (int i = 0; i < filterInventory.size(); i++) {
            ItemStack filterStack = filterInventory.getStack(i);
            if (!filterStack.isEmpty() && ItemStack.areItemsEqual(filterStack, stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFilterEmpty() {
        for (int i = 0; i < filterInventory.size(); i++) {
            if (!filterInventory.getStack(i).isEmpty()) return false;
        }
        return true;
    }

    // =========================================================================
    // SCREEN HANDLER FACTORY
    // =========================================================================

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.glass_pipe_transport.filter_pipe");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory,
                                               PlayerEntity player) {
        return new FilterPipeScreenHandler(syncId, playerInventory, this);
    }

    // =========================================================================
    // NBT SERIALIZATION
    // =========================================================================

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putBoolean("whitelistMode", whitelistMode);

        DefaultedList<ItemStack> filterItems = DefaultedList.ofSize(9, ItemStack.EMPTY);
        for (int i = 0; i < filterInventory.size(); i++) {
            filterItems.set(i, filterInventory.getStack(i));
        }
        nbt.put("filterInventory", Inventories.writeNbt(new NbtCompound(), filterItems, registries));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        whitelistMode = nbt.getBoolean("whitelistMode");

        DefaultedList<ItemStack> filterItems = DefaultedList.ofSize(9, ItemStack.EMPTY);
        Inventories.readNbt(nbt.getCompound("filterInventory"), filterItems, registries);
        for (int i = 0; i < filterItems.size(); i++) {
            filterInventory.setStack(i, filterItems.get(i));
        }
    }

    // =========================================================================
    // GETTERS / SETTERS
    // =========================================================================

    public SimpleInventory getFilterInventory() { return filterInventory; }
    public boolean isWhitelistMode() { return whitelistMode; }
    public void setWhitelistMode(boolean whitelist) {
        this.whitelistMode = whitelist;
        markDirty();
    }
    public void toggleMode() {
        whitelistMode = !whitelistMode;
        markDirty();
    }
}
