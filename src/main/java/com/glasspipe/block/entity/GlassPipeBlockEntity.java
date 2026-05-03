package com.glasspipe.block.entity;

import com.glasspipe.GlassPipeTransportMod;
import com.glasspipe.block.GlassPipeBlock;
import com.glasspipe.transport.PipePathfinder;
import com.glasspipe.transport.TransitItem;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

/**
 * Block entity for the glass pipe. Manages in-transit items,
 * handles item extraction from adjacent inventories, and routes
 * items toward their destinations.
 */
public class GlassPipeBlockEntity extends BlockEntity {

    /** Items currently moving through this pipe */
    protected final List<TransitItem> transitItems = new ArrayList<>();

    /** Upgrade flags */
    private boolean hasSpeedUpgrade = false;
    private boolean hasSortingUpgrade = false;

    /** Tick counter for extraction attempts */
    private int extractionCooldown = 0;
    private static final int EXTRACTION_INTERVAL = 10;

    /** Client-side: items for rendering (synced from server) */
    public final List<TransitItem> clientTransitItems = new ArrayList<>();

    public GlassPipeBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.GLASS_PIPE, pos, state);
    }

    protected GlassPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // =========================================================================
    // SERVER TICK
    // =========================================================================

    public static void serverTick(World world, BlockPos pos, BlockState state,
                                   GlassPipeBlockEntity pipe) {
        pipe.serverTickInternal(world, pos, state);
    }

    protected void serverTickInternal(World world, BlockPos pos, BlockState state) {
        // 1. Advance all in-transit items
        List<TransitItem> toRemove = new ArrayList<>();

        for (TransitItem item : transitItems) {
            boolean arrived = item.tick();
            if (arrived) {
                handleItemArrival(world, pos, state, item, toRemove);
            }
        }

        transitItems.removeAll(toRemove);

        // 2. Periodically try to extract items from adjacent inventories
        extractionCooldown--;
        if (extractionCooldown <= 0) {
            extractionCooldown = EXTRACTION_INTERVAL;
            tryExtractFromNeighbors(world, pos, state);
        }

        // 3. Sync to client if we have items in transit
        if (!transitItems.isEmpty()) {
            markDirty();
            world.updateListeners(pos, state, state, 3);
        }
    }

    /**
     * Called when a transit item reaches progress 1.0 (end of this pipe segment).
     * Either passes it to the next pipe or inserts it into the target inventory.
     */
    private void handleItemArrival(World world, BlockPos pos, BlockState state,
                                    TransitItem item, List<TransitItem> toRemove) {
        Direction dir = item.getTravelDirection();
        BlockPos nextPos = pos.offset(dir);
        BlockState nextState = world.getBlockState(nextPos);

        // Case 1: Next block is a pipe — pass the item along
        if (nextState.getBlock() instanceof com.glasspipe.block.GlassPipeBlock) {
            BlockEntity nextBe = world.getBlockEntity(nextPos);
            if (nextBe instanceof GlassPipeBlockEntity nextPipe) {
                Direction nextDir = findDirectionToward(world, nextPos, nextState,
                        dir.getOpposite(), item.getTargetPos());

                if (nextDir != null) {
                    float speed = nextPipe.hasSpeedUpgrade ? 2.0f : 1.0f;
                    TransitItem newItem = new TransitItem(item.getStack(), nextDir,
                            item.getTargetPos(), speed);
                    nextPipe.transitItems.add(newItem);
                    nextPipe.markDirty();
                    toRemove.add(item);
                    GlassPipeTransportMod.LOGGER.debug(
                            "Item {} passed from {} to {} heading {}",
                            item.getStack().getItem(), pos, nextPos, nextDir);
                    return;
                }
            }
        }

        // Case 2: Next block is the target inventory — try to insert
        BlockEntity targetBe = world.getBlockEntity(nextPos);
        if (targetBe != null) {
            Storage<ItemVariant> storage = ItemStorage.SIDED.find(
                    world, nextPos, nextState, targetBe, dir.getOpposite());
            if (storage != null && storage.supportsInsertion()) {
                try (Transaction tx = Transaction.openOuter()) {
                    long inserted = storage.insert(
                            ItemVariant.of(item.getStack()),
                            item.getStack().getCount(), tx);
                    if (inserted > 0) {
                        tx.commit();
                        toRemove.add(item);
                        GlassPipeTransportMod.LOGGER.debug(
                                "Item {} delivered to inventory at {}",
                                item.getStack().getItem(), nextPos);
                        return;
                    }
                }
            }
        }

        // Case 3: Can't proceed — item is stuck, try rerouting
        item.incrementStuckTicks();
        if (item.isStuck()) {
            GlassPipeTransportMod.LOGGER.debug(
                    "Item {} stuck at {}, attempting reroute", item.getStack().getItem(), pos);
            PipePathfinder.PathResult result = PipePathfinder.findDestination(
                    world, pos, item.getStack(), dir.getOpposite(), hasSortingUpgrade);
            if (result.found()) {
                item.setTravelDirection(result.firstStep());
                item.setTargetPos(result.targetPos());
                item.resetStuckTicks();
            } else {
                // No path found — drop the item
                ItemScatterer.spawn(world,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        item.getStack());
                toRemove.add(item);
                GlassPipeTransportMod.LOGGER.debug(
                        "Item {} dropped at {} (no valid destination)", item.getStack().getItem(), pos);
            }
        }
    }

    /**
     * BFS to find which connected direction from 'from' leads toward 'target'.
     */
    protected Direction findDirectionToward(World world, BlockPos from, BlockState fromState,
                                             Direction cameFrom, BlockPos target) {
        List<Direction> connected = GlassPipeBlock.getConnectedDirections(fromState);

        // Direct adjacency check first
        for (Direction dir : connected) {
            if (dir == cameFrom) continue;
            if (from.offset(dir).equals(target)) return dir;
        }

        // BFS to find which direction leads to target
        for (Direction dir : connected) {
            if (dir == cameFrom) continue;
            BlockPos neighbor = from.offset(dir);
            if (world.getBlockState(neighbor).getBlock() instanceof GlassPipeBlock) {
                if (bfsCanReach(world, neighbor, dir.getOpposite(), target, 128)) {
                    return dir;
                }
            }
        }

        // Fallback: any connected direction that isn't where we came from
        for (Direction dir : connected) {
            if (dir != cameFrom) return dir;
        }
        return null;
    }

    private boolean bfsCanReach(World world, BlockPos start, Direction cameFrom,
                                  BlockPos target, int maxNodes) {
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && visited.size() < maxNodes) {
            BlockPos current = queue.poll();
            if (current.equals(target)) return true;

            BlockState state = world.getBlockState(current);
            if (!(state.getBlock() instanceof GlassPipeBlock)) {
                // It's an inventory — check if it's the target
                continue;
            }

            for (Direction dir : GlassPipeBlock.getConnectedDirections(state)) {
                BlockPos next = current.offset(dir);
                if (!visited.contains(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return false;
    }

    /**
     * Tries to pull items from adjacent inventories into the pipe network.
     */
    protected void tryExtractFromNeighbors(World world, BlockPos pos, BlockState state) {
        List<Direction> connected = GlassPipeBlock.getConnectedDirections(state);

        for (Direction dir : connected) {
            BlockPos neighborPos = pos.offset(dir);
            BlockState neighborState = world.getBlockState(neighborPos);

            // Don't extract from other pipes
            if (neighborState.getBlock() instanceof GlassPipeBlock) continue;

            BlockEntity neighborBe = world.getBlockEntity(neighborPos);
            if (neighborBe == null) continue;

            Storage<ItemVariant> storage = ItemStorage.SIDED.find(
                    world, neighborPos, neighborState, neighborBe, dir.getOpposite());
            if (storage == null || !storage.supportsExtraction()) continue;

            // Try to extract one item at a time
            try (Transaction simulateTx = Transaction.openOuter()) {
                for (var view : storage) {
                    if (view.isResourceBlank()) continue;
                    ItemStack candidate = view.getResource().toStack(1);

                    // Find a destination for this item
                    PipePathfinder.PathResult result = PipePathfinder.findDestination(
                            world, pos, candidate, dir, hasSortingUpgrade);

                    if (!result.found()) continue;

                    // Commit the extraction
                    simulateTx.abort();
                    try (Transaction commitTx = Transaction.openOuter()) {
                        long extracted = view.extract(view.getResource(), 1, commitTx);
                        if (extracted > 0) {
                            commitTx.commit();
                            float speed = hasSpeedUpgrade ? 2.0f : 1.0f;
                            TransitItem transit = new TransitItem(
                                    view.getResource().toStack((int) extracted),
                                    result.firstStep(),
                                    result.targetPos(),
                                    speed
                            );
                            transitItems.add(transit);
                            markDirty();
                            GlassPipeTransportMod.LOGGER.debug(
                                    "Extracted {} from {} at {}, routing to {}",
                                    candidate.getItem(), dir, pos, result.targetPos());
                        }
                    }
                    return; // One item per extraction cycle
                }
                simulateTx.abort();
            }
        }
    }

    // =========================================================================
    // CLIENT TICK
    // =========================================================================

    public static void clientTick(World world, BlockPos pos, BlockState state,
                                   GlassPipeBlockEntity pipe) {
        for (TransitItem item : pipe.clientTransitItems) {
            item.tick();
        }
    }

    // =========================================================================
    // NBT SERIALIZATION
    // =========================================================================

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putBoolean("hasSpeedUpgrade", hasSpeedUpgrade);
        nbt.putBoolean("hasSortingUpgrade", hasSortingUpgrade);

        NbtList itemList = new NbtList();
        for (TransitItem item : transitItems) {
            itemList.add(item.toNbt(registries));
        }
        nbt.put("transitItems", itemList);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        hasSpeedUpgrade = nbt.getBoolean("hasSpeedUpgrade");
        hasSortingUpgrade = nbt.getBoolean("hasSortingUpgrade");

        transitItems.clear();
        NbtList itemList = nbt.getList("transitItems", NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < itemList.size(); i++) {
            transitItems.add(TransitItem.fromNbt(itemList.getCompound(i), registries));
        }

        // Sync to client list
        clientTransitItems.clear();
        clientTransitItems.addAll(transitItems);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    // =========================================================================
    // PUBLIC API
    // =========================================================================

    public List<ItemStack> getTransitItems() {
        List<ItemStack> stacks = new ArrayList<>();
        for (TransitItem item : transitItems) {
            stacks.add(item.getStack().copy());
        }
        return stacks;
    }

    public List<TransitItem> getTransitItemObjects() {
        return Collections.unmodifiableList(transitItems);
    }

    public boolean hasSpeedUpgrade() { return hasSpeedUpgrade; }
    public void setSpeedUpgrade(boolean value) { this.hasSpeedUpgrade = value; }

    public boolean hasSortingUpgrade() { return hasSortingUpgrade; }
    public void setSortingUpgrade(boolean value) { this.hasSortingUpgrade = value; }
}
