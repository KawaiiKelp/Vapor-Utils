package com.kawaiikelp.vapor_utils.block;

import com.kawaiikelp.vapor_utils.block.entity.DryingRackBlockEntity;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class DryingRackBlock extends BaseEntityBlock implements EntityBlock, SimpleWaterloggedBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // 히트박스 회전시키기 귀찮지? 내가 다 계산해놨어♡
    // North 기준: 0, 12, 12 -> 16, 16, 16 (벽 쪽에 붙은 선반 가정)
    // 만약 아저씨 모델이 천장 중앙형이면 회전 필요 없을 수도 있는데, 일단 벽걸이형이라 치고!
    private static final VoxelShape SHAPE_NORTH = Block.box(0, 12, 12, 16, 16, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 12, 0, 16, 16, 4);
    private static final VoxelShape SHAPE_EAST = Block.box(0, 12, 0, 4, 16, 16);
    private static final VoxelShape SHAPE_WEST = Block.box(12, 12, 0, 16, 16, 16);

    public DryingRackBlock(Properties properties) {
        super(properties);
        // 기본값 설정: 북쪽 보고, 물 없음
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    // 1. 프로퍼티 등록
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    // 2. 설치될 때 상태 결정 (플레이어 보는 방향 반대 & 물 속에 설치했나?)
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    // 3. 모양(히트박스) 돌려주기
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH ;
        };
    }

    // 4. 물 관련 처리 (물이 흐르거나 빠질 때)
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    // 이 블록이 설치되면 엔티티도 같이 만들어! 라고 명령
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    // 블록이 투명해지지 않게
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // 우클릭 했을 때!
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DryingRackBlockEntity rack) {
                ItemStackHandler handler = rack.getItemHandler();
                ItemStack heldItem = player.getItemInHand(hand);
                ItemStack storedItem = handler.getStackInSlot(0);

                if (storedItem.isEmpty() && !heldItem.isEmpty()) {
                    // 빈 건조대에 아이템 넣기
                    ItemStack remaining = handler.insertItem(0, heldItem, false);
                    player.setItemInHand(hand, remaining);
                    return InteractionResult.SUCCESS;
                } else if (!storedItem.isEmpty() && heldItem.isEmpty()) {
                    // 맨손으로 클릭해서 아이템 넣기
                    player.setItemInHand(hand, handler.extractItem(0, 64, false));
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity>BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // 서버 월드일 때만, 그리고 우리 블록 엔티티가 맞을 때만 'tick' 메서드를 실행해라!
        if (level.isClientSide) {
            return null;
        }

        // createTickerHelper는 Forge가 제공하는 편리한 도구야. 타입 안 맞으면 null 뱉어줌.
        return createTickerHelper(type, VaporUtilsBlockEntities.DRYING_RACK_BE.get(), DryingRackBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // 1. 블록이 아예 바뀌는 경우에만 실행 (상태 변경 제외)
        if (!state.is(newState.getBlock())) {

            // 2. 엔티티 가져오기
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof DryingRackBlockEntity rack) {
                // 3. 아이템 꺼내서 바닥에 떨구기!
                // (getItemHandler() 메서드 아까 만들었지? 없으면 빨간줄 뜬다?)
                ItemStack stack = rack.getItemHandler().getStackInSlot(0);
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                }
            }

            // 4. 기본 동작 (엔티티 삭제 등)
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
