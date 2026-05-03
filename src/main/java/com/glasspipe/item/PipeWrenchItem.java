package com.glasspipe.item;

import com.glasspipe.block.entity.GlassPipeBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Pipe Wrench - used to retrieve installed upgrades from pipes without breaking them.
 * Sneak + right-click to remove speed upgrade.
 * Right-click to remove sorting upgrade.
 */
public class PipeWrenchItem extends Item {

    public PipeWrenchItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof GlassPipeBlockEntity pipe) {
            boolean sneaking = player != null && player.isSneaking();

            if (sneaking && pipe.hasSpeedUpgrade()) {
                pipe.setSpeedUpgrade(false);
                pipe.markDirty();
                if (player != null) {
                    if (!player.isCreative()) {
                        ItemStack upgrade = new ItemStack(ModItems.SPEED_UPGRADE);
                        player.getInventory().offerOrDrop(upgrade);
                    }
                    player.sendMessage(
                            Text.translatable("item.glass_pipe_transport.pipe_wrench.removed_speed"),
                            true
                    );
                }
                return ActionResult.SUCCESS;
            } else if (!sneaking && pipe.hasSortingUpgrade()) {
                pipe.setSortingUpgrade(false);
                pipe.markDirty();
                if (player != null) {
                    if (!player.isCreative()) {
                        ItemStack upgrade = new ItemStack(ModItems.SORTING_UPGRADE);
                        player.getInventory().offerOrDrop(upgrade);
                    }
                    player.sendMessage(
                            Text.translatable("item.glass_pipe_transport.pipe_wrench.removed_sorting"),
                            true
                    );
                }
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.glass_pipe_transport.pipe_wrench");
    }
}
