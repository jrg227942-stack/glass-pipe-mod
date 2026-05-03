package com.glasspipe.transport;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Represents an item currently in transit through the pipe network.
 * Tracks position progress (0.0 to 1.0) through the current pipe segment,
 * the destination direction, and the full path to the target inventory.
 */
public class TransitItem {

    private ItemStack stack;
    /** Progress through the current pipe: 0.0 = just entered, 1.0 = ready to exit */
    private float progress;
    /** The direction this item is traveling toward (exit side of current pipe) */
    private Direction travelDirection;
    /** The target inventory position (final destination) */
    private BlockPos targetPos;
    /** Speed multiplier (1.0 = normal, 2.0 = double speed with upgrade) */
    private float speedMultiplier;
    /** Ticks this item has been stuck (for deadlock detection) */
    private int stuckTicks;

    public TransitItem(ItemStack stack, Direction travelDirection, BlockPos targetPos, float speedMultiplier) {
        this.stack = stack.copy();
        this.progress = 0.0f;
        this.travelDirection = travelDirection;
        this.targetPos = targetPos;
        this.speedMultiplier = speedMultiplier;
        this.stuckTicks = 0;
    }

    /**
     * Advances the item's progress through the pipe.
     * @return true if the item has reached the exit (progress >= 1.0)
     */
    public boolean tick() {
        progress += 0.05f * speedMultiplier;
        if (progress >= 1.0f) {
            progress = 1.0f;
            return true;
        }
        return false;
    }

    public void incrementStuckTicks() {
        stuckTicks++;
    }

    public void resetStuckTicks() {
        stuckTicks = 0;
    }

    public boolean isStuck() {
        return stuckTicks > 40; // 2 seconds at 20 TPS
    }

    // --- Serialization ---

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = new NbtCompound();
        nbt.getCompoundOrEmpty("stack").
        nbt.putFloat("progress", progress);
        nbt.putString("direction", travelDirection.getName());
        nbt.putLong("targetPos", targetPos.asLong());
        nbt.putFloat("speedMultiplier", speedMultiplier);
        nbt.putInt("stuckTicks", stuckTicks);
        return nbt;
    }

    public static TransitItem fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        ItemStack stack = ItemStack.fromNbtOrEmpty(registries, nbt.getCompound("stack"));
        Direction dir = Direction.byName(nbt.getString("direction"));
        if (dir == null) dir = Direction.NORTH;
        BlockPos target = BlockPos.fromLong(nbt.getLong("targetPos"));
        float speed = nbt.getFloat("speedMultiplier");
        TransitItem item = new TransitItem(stack, dir, target, speed);
        item.progress = nbt.getFloat("progress");
        item.stuckTicks = nbt.getInt("stuckTicks");
        return item;
    }

    // --- Getters / Setters ---

    public ItemStack getStack() { return stack; }
    public float getProgress() { return progress; }
    public Direction getTravelDirection() { return travelDirection; }
    public void setTravelDirection(Direction dir) { this.travelDirection = dir; }
    public BlockPos getTargetPos() { return targetPos; }
    public void setTargetPos(BlockPos pos) { this.targetPos = pos; }
    public float getSpeedMultiplier() { return speedMultiplier; }
    public void setSpeedMultiplier(float speed) { this.speedMultiplier = speed; }
    public int getStuckTicks() { return stuckTicks; }
}
