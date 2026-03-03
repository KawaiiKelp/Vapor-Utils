package com.kawaiikelp.vapor_utils.block;

import com.kawaiikelp.vapor_utils.block.entity.BlueprintWorkbenchBlockEntity;
import com.kawaiikelp.vapor_utils.client.gui.menu.BlueprintWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class BlueprintWorkbenchBlock extends BaseEntityBlock {
    // 1. 방향 속성 정의! (수평 방향만)
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlueprintWorkbenchBlock(Properties properties) {
        super(properties);
        // 2. 기본 상태는 북쪽으로 세팅♡
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    // 3. 블록에 방향 속성 등록
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // 4. 아저씨가 블록을 설치할 때! 아저씨를 바라보게 설치해 줌♡
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // 이거 안 하면 블록이 투명인간 된단다♡
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlueprintWorkbenchBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // 서버에서만 GUI를 열어야 해!
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlueprintWorkbenchBlockEntity workbench) {
                // [진짜 GUI 열기 로직]
                // 1. id: 윈도우 ID
                // 2. inv: 플레이어 인벤토리
                // 3. workbench: 우리 작업대 엔티티
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                        (id, inv, p) -> new BlueprintWorkbenchMenu(id, inv, workbench),
                        Component.translatable("gui.vapor_utils.blueprint_workbench")
                ), pos); // 마지막 pos는 클라이언트에 좌표 전달용이야♡

                // [추가] 창 열어준 직후에 파일 목록 쏴주기!
                workbench.syncFilesToPlayer(serverPlayer);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // 블록 부서질 때 아이템 드랍!
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // 블록이 완전히 다른 걸로 바뀌었을 때만 (상태 업데이트 제외)
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BlueprintWorkbenchBlockEntity workbench) {
                // 슬롯 2개(0, 1) 다 뒤져서 아이템 있으면 바닥에 던짐♡
                for (int i = 0; i < workbench.inventory.getSlots(); i++) {
                    net.minecraft.world.item.ItemStack stack = workbench.inventory.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        net.minecraft.world.Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                    }
                }
            }
            // 부모 클래스의 onRemove 호출해서 뒷정리
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}