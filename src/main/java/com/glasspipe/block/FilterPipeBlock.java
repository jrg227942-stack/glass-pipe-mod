package com.glasspipe.block;

import com.glasspipe.block.entity.FilterPipeBlockEntity;
import com.glasspipe.block.entity.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FilterPipeBlock extends GlassPipeBlock {

    public static final MapCodec<FilterPipeBlock> CODEC = createCodec(FilterPipeBlock::new);

    public FilterPipeBlock(Settings settings) {
        super(settings);
    }

    @Override
    public MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FilterPipeBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.FILTER_PIPE) return null;
        if (world.isClient()) {
            return (w, pos, s, be) -> FilterPipeBlockEntity.clientTick(w, pos, s, (FilterPipeBlockEntity) be);
        }
        return (w, pos, s, be) -> FilterPipeBlockEntity.serverTick(w, pos, s, (FilterPipeBlockEntity) be);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                               PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof NamedScreenHandlerFactory factory) {
                player.openHandledScreen(factory);
            }
        }
        return ActionResult.SUCCESS;
    }
}
