package com.glasspipe.transport;

import com.glasspipe.block.FilterPipeBlock;
import com.glasspipe.block.GlassPipeBlock;
import com.glasspipe.block.entity.FilterPipeBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

/**
 * BFS-based pathfinder for the pipe network.
 * Finds the nearest valid inventory that can accept a given item,
 * respecting filter rules along the path.
 */
public class PipePathfinder {

    /**
     * Result of a pathfinding operation.
     */
    public record PathResult(BlockPos targetPos, Direction firstStep, boolean found) {}

    /**
     * Finds the best destination for an item starting from the given pipe position.
     *
     * @param world       The world
     * @param startPos    The pipe position the item is currently in
     * @param stack       The item to route
     * @param fromDir     The direction the item came from (to avoid going back)
     * @param useSorting  Whether to use smart sorting (find best-fit inventory)
     * @return PathResult with the target position and first step direction
     */
    public static PathResult findDestination(World world, BlockPos startPos,
                                              ItemStack stack, Direction fromDir,
                                              boolean useSorting) {
        // BFS: maps each visited position to the first-step direction taken from startPos
        Queue<BlockPos> queue = new LinkedList<>();
        Map<BlockPos, Direction> firstStepMap = new HashMap<>();
        Set<BlockPos> visited = new HashSet<>();

        BlockState startState = world.getBlockState(startPos);
        List<Direction> startDirs = GlassPipeBlock.getConnectedDirections(startState);

        // Seed BFS with all connected neighbors except where we came from
        for (Direction dir : startDirs) {
            if (dir == fromDir) continue;
            BlockPos neighbor = startPos.offset(dir);
            if (visited.add(neighbor)) {
                queue.add(neighbor);
                firstStepMap.put(neighbor, dir);
            }
        }

        BlockPos bestInventory = null;
        Direction bestFirstStep = null;
        long bestInsertable = 0;

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockState currentState = world.getBlockState(current);
            Direction stepDir = firstStepMap.get(current);

            if (currentState.getBlock() instanceof GlassPipeBlock) {
                // Check filter rules if it's a filter pipe
                if (currentState.getBlock() instanceof FilterPipeBlock) {
                    BlockEntity be = world.getBlockEntity(current);
                    if (be instanceof FilterPipeBlockEntity filterPipe) {
                        if (!stack.isEmpty() && !filterPipe.allowsItem(stack)) {
                            continue; // Item blocked by filter — skip this branch
                        }
                    }
                }

                // Continue BFS through connected pipe directions
                for (Direction dir : GlassPipeBlock.getConnectedDirections(currentState)) {
                    BlockPos neighbor = current.offset(dir);
                    if (visited.add(neighbor)) {
                        queue.add(neighbor);
                        firstStepMap.put(neighbor, stepDir);
                    }
                }
            } else {
                // Not a pipe — check if it's an inventory that can accept the item
                // Determine which face of this block the pipe connects to
                Direction insertFace = findInsertFace(world, current, visited);
                if (insertFace == null) insertFace = Direction.NORTH;

                BlockEntity be = world.getBlockEntity(current);
                if (be != null) {
                    Storage<ItemVariant> storage = ItemStorage.SIDED.find(
                            world, current, currentState, be, insertFace);
                    if (storage != null && storage.supportsInsertion()) {
                        long insertable = stack.isEmpty() ? 1 :
                                StorageUtil.simulateInsert(storage,
                                        ItemVariant.of(stack), stack.getCount(), null);
                        if (insertable > 0) {
                            if (!useSorting) {
                                // Return first valid destination found
                                return new PathResult(current, stepDir, true);
                            }
                            // With sorting, find the inventory that can accept the most
                            if (insertable > bestInsertable) {
                                bestInsertable = insertable;
                                bestInventory = current;
                                bestFirstStep = stepDir;
                            }
                        }
                    }
                }
            }
        }

        if (bestInventory != null) {
            return new PathResult(bestInventory, bestFirstStep, true);
        }

        return new PathResult(null, null, false);
    }

    /**
     * Finds the face direction from which a non-pipe block is being approached
     * by looking at which visited pipe neighbor connects to it.
     */
    private static Direction findInsertFace(World world, BlockPos targetPos,
                                             Set<BlockPos> visited) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = targetPos.offset(dir);
            if (visited.contains(neighbor)) {
                BlockState neighborState = world.getBlockState(neighbor);
                if (neighborState.getBlock() instanceof GlassPipeBlock) {
                    return dir.getOpposite();
                }
            }
        }
        return null;
    }
}
