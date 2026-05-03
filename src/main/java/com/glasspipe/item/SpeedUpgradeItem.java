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
 * Speed Upgrade item - when used on a glass pipe, increases item transport speed.
 * Each pipe can hold one upgrade. Speed upgrades stack multiplicatively with
 * adjacent upgraded pipes.
 */
public class SpeedUpgradeItem extends Item {

    public SpeedUpgradeItem(Settings settings) {
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
            if (!pipe.hasSpeedUpgrade()) {
                pipe.setSpeedUpgrade(true);
                pipe.markDirty();

                if (player != null) {
                    if (!player.isCreative()) {
                        context.getStack().decrement(1);
                    }
                    player.sendMessage(
                            Text.translatable("item.glass_pipe_transport.speed_upgrade.applied"),
                            true
                    );
                }
                return ActionResult.SUCCESS;
            } else {
                if (player != null) {
                    player.sendMessage(
                            Text.translatable("item.glass_pipe_transport.speed_upgrade.already_installed"),
                            true
                    );
                }
                return ActionResult.FAIL;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("item.glass_pipe_transport.speed_upgrade");
    }
}
