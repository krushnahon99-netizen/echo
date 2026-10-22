package com.echotechnology.block;

import com.echotechnology.block.entity.EchoConduitBlockEntity;
import com.echotechnology.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/** Energy conduit that visually connects to other conduits and Echo machines. */
public class EchoConduitBlock extends BlockWithEntity {
    public static final MapCodec<EchoConduitBlock> CODEC = createCodec(EchoConduitBlock::new);
    private static final Map<Direction, BooleanProperty> CONNECTIONS = ConnectingBlock.FACING_PROPERTIES;
    private static final VoxelShape CORE = Block.createCuboidShape(5, 5, 5, 11, 11, 11);
    private static final Map<Direction, VoxelShape> ARMS = new EnumMap<>(Direction.class);
    private final Map<BlockState, VoxelShape> shapeCache = new HashMap<>();

    static {
        ARMS.put(Direction.NORTH, Block.createCuboidShape(6, 6, 0, 10, 10, 5));
        ARMS.put(Direction.SOUTH, Block.createCuboidShape(6, 6, 11, 10, 10, 16));
        ARMS.put(Direction.WEST, Block.createCuboidShape(0, 6, 6, 5, 10, 10));
        ARMS.put(Direction.EAST, Block.createCuboidShape(11, 6, 6, 16, 10, 10));
        ARMS.put(Direction.DOWN, Block.createCuboidShape(6, 0, 6, 10, 5, 10));
        ARMS.put(Direction.UP, Block.createCuboidShape(6, 11, 6, 10, 16, 10));
    }

    public EchoConduitBlock(Settings settings) {
        super(settings);
        BlockState state = getStateManager().getDefaultState();
        for (BooleanProperty property : CONNECTIONS.values()) {
            state = state.with(property, false);
        }
        setDefaultState(state);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CONNECTIONS.values().toArray(new BooleanProperty[0]));
    }

    public static boolean connectsTo(BlockState neighbour) {
        return neighbour.getBlock() instanceof EchoConduitBlock || neighbour.getBlock() instanceof MachineBlock;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = getDefaultState();
        for (Direction direction : Direction.values()) {
            BlockState neighbour = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(direction));
            state = state.with(CONNECTIONS.get(direction), connectsTo(neighbour));
        }
        return state;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                   WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return state.with(CONNECTIONS.get(direction), connectsTo(neighborState));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shapeCache.computeIfAbsent(state, s -> {
            VoxelShape shape = CORE;
            for (Map.Entry<Direction, BooleanProperty> entry : CONNECTIONS.entrySet()) {
                if (s.get(entry.getValue())) {
                    shape = VoxelShapes.union(shape, ARMS.get(entry.getKey()));
                }
            }
            return shape;
        });
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EchoConduitBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : validateTicker(type, ModBlockEntities.ECHO_CONDUIT, EchoConduitBlockEntity::serverTick);
    }
}
