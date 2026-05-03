package com.glasspipe.block;

import com.glasspipe.block.entity.GlassPipeBlockEntity;
import com.glasspipe.block.entity.ModBlockEntities;
import com.glasspipe.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GlassPipeBlock extends BlockWithEntity {

    public static final MapCodec<GlassPipeBlock> CODEC = createCodec(GlassPipeBlock::new);

    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty EAST  = Properties.EAST;
    public static final BooleanProperty WEST  = Properties.WEST;
    public static final BooleanProperty UP    = Properties.UP;
    public static final BooleanProperty DOWN  = Properties.DOWN;

    protected static final VoxelShape CORE   = Block.createCuboidShape(4, 4, 4, 12, 12, 12);
    protected static final VoxelShape ARM_N  = Block.createCuboidShape(4, 4,  0, 12, 12,  4);
    protected static final VoxelShape ARM_S  = Block.createCuboidShape(4, 4, 12, 12, 12, 16);
    protected static final VoxelShape ARM_E  = Block.createCuboidShape(12, 4, 4, 16, 12, 12);
    protected static final VoxelShape ARM_W  = Block.createCuboidShape( 0, 4, 4,  4, 12, 12);
    protected static final VoxelShape ARM_UP = Block.createCuboidShape(4, 12, 4, 12, 16, 12);
    protected static final VoxelShape ARM_DN = Block.createCuboidShape(4,  0, 4, 12,  4, 12);

    public GlassPipeBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(NORTH, false).with(SOUTH, false)
                .with(EAST,  false).with(WEST,  false)
                .with(UP,    false).with(DOWN,  false));
    }

    @Override
    public MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = CORE;
        if (state.get(NORTH)) shape = VoxelShapes.union(shape, ARM_N);
        if (state.get(SOUTH)) shape = VoxelShapes.union(shape, ARM_S);
        if (state.get(EAST))  shape = VoxelShapes.union(shape, ARM_E);
        if (state.get(WEST))  shape = VoxelShapes.union(shape, ARM_W);
        if (state.get(UP))    shape = VoxelShapes.union(shape, ARM_UP);
        if (state.get(DOWN))  shape = VoxelShapes.union(shape, ARM_DN);
        return shape;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        return getDefaultState()
                .with(NORTH, canConnect(world, pos, Direction.NORTH))
                .with(SOUTH, canConnect(world, pos, Direction.SOUTH))
                .with(EAST,  canConnect(world, pos, Direction.EAST))
                .with(WEST,  canConnect(world, pos, Direction.WEST))
                .with(UP,    canConnect(world, pos, Direction.UP))
                .with(DOWN,  canConnect(world, pos, Direction.DOWN));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction,
                                                 BlockState neighborState, WorldAccess world,
                                                 BlockPos pos, BlockPos neighborPos) {
        return state.with(directionToProperty(direction), canConnect(world, pos, direction));
    }

    protected boolean canConnect(BlockView world, BlockPos pos, Direction dir) {
        BlockPos neighborPos = pos.offset(dir);
        BlockState neighborState = world.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof GlassPipeBlock) return true;
        BlockEntity be = world.getBlockEntity(neighborPos);
        if (be != null && world instanceof World w) {
            return ItemStorage.SIDED.find(w, neighborPos, neighborState, be, dir.getOpposite()) != null;
        }
        return false;
    }

    public BooleanProperty directionToProperty(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST  -> EAST;
            case WEST  -> WEST;
            case UP    -> UP;
            case DOWN  -> DOWN;
        };
    }

    public static List<Direction> getConnectedDirections(BlockState state) {
        List<Direction> dirs = new ArrayList<>();
        if (state.get(NORTH)) dirs.add(Direction.NORTH);
        if (state.get(SOUTH)) dirs.add(Direction.SOUTH);
        if (state.get(EAST))  dirs.add(Direction.EAST);
        if (state.get(WEST))  dirs.add(Direction.WEST);
        if (state.get(UP))    dirs.add(Direction.UP);
        if (state.get(DOWN))  dirs.add(Direction.DOWN);
        return dirs;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GlassPipeBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) {
            return checkType(type, ModBlockEntities.GLASS_PIPE, GlassPipeBlockEntity::clientTick);
        }
        return checkType(type, ModBlockEntities.GLASS_PIPE, GlassPipeBlockEntity::serverTick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                               PlayerEntity player, BlockHitResult hit) {
        return ActionResult.PASS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos,
                                 BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof GlassPipeBlockEntity pipe) {
                for (ItemStack stack : pipe.getTransitItems()) {
                    ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                }
                if (pipe.hasSpeedUpgrade()) {
                    ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.SPEED_UPGRADE));
                }
                if (pipe.hasSortingUpgrade()) {
                    ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.SORTING_UPGRADE));
                }
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }
}

