package com.kawaiikelp.vapor_utils.block.entity;

import com.kawaiikelp.vapor_utils.recipe.DryingRecipe;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsBlockEntities;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DryingRackBlockEntity extends BlockEntity {

    // 진행도 (얼마나 말랐는지)
    private int progress = 0;

    // [여기!] 이거 없어서 빨간 줄 뜨는 거잖아, 멍청아!
    private int totalTicks = 0;

    public int getProgress() {
        return this.progress;
    }

    public int getTotalTicks() {
        return this.totalTicks;
    }

    // 아이템을 1개만 저장하는 핸들러
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            // 1. 아이템이 바뀌면 저장하라고 신호 보냄
            setChanged();

            // 2. 클라이언트한테 "야! 다시 그려!" 하고 신호 보내기 (동기화용)
            if (level != null && !level.isClientSide) {
                // 블록 상태를 갱신해서 클라이언트가 다시 렌더링하게
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        // [여기가 핵심!] 이 슬롯에 아이템이 들어갈 수 있나요?
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            // 1. 레벨이 없으면 모른 척 (안 넣어줌)
            if (level == null) return false;

            // 2. 이 아이템으로 만들 수 있는 'DryingRecipe'가 하나라도 있는지 확인!
            //    (SimpleContainer로 아이템을 감싸서 레시피 매니저에게 물어보는 거야)
            return level.getRecipeManager()
                    .getRecipeFor(VaporUtilsRecipes.DRYING_TYPE.get(), new net.minecraft.world.SimpleContainer(stack), level)
                    .isPresent();
        }
    };

    // 1. 서버 -> 클라이언트 : "야, 이 블록 정보 받아라!" (업데이트 패킷 생성)
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // 2. 서버 -> 클라이언트 : "처음 로딩할 때도 정보 챙겨가라!" (NBT 태그 생성)
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    // 3. 클라이언트 : "어? 서버에서 편지 왔네? 읽어야지!" (패킷 수신 및 적용)
    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        // 서버가 보낸 NBT 데이터를 내 데이터에 덮어씌우기
        // 이렇게 해야 클라이언트 쪽 아이템 핸들러에도 아이템이 생겨.
        load(pkt.getTag());
    }

    private final LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(VaporUtilsBlockEntities.DRYING_RACK_BE.get(), pos, state);
    }

    // 저장 - 게임 껐다 켜도 아이템 안 사라지게♡
    // [중요 1] 저장할 때 진행도 까먹으면 안 돼!
    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("progress", progress);
        super.saveAdditional(tag);
    }

    // 로드
    // [중요 2] 불러올 때도 기억해내야지!
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
    }

    // [핵심] 매 틱마다 실행될 로직
    public static void tick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity entity) {
        // 클라이언트는 계산 안 해. 서버만 일해!
        if (level.isClientSide) return;

        ItemStack stack = entity.itemHandler.getStackInSlot(0);

        // 1. 아이템이 없으면 진행도 초기화하고 끝
        if (stack.isEmpty()) {
            entity.resetProgress();
            entity.totalTicks = 0;
            return;
        }

        // 2. 현재 아이템에 맞는 레시피 찾기
        Optional<DryingRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(VaporUtilsRecipes.DRYING_TYPE.get(), new SimpleContainer(stack), level);

        if (recipe.isPresent()) {
            DryingRecipe match = recipe.get();
            entity.totalTicks = match.getTicks();

            // 3. 시간 흐름
            entity.progress++;

            // 4. 건조 완료?
            if (entity.progress >= match.getTicks()) {
                // 결과물 만들기
                ItemStack result = match.assemble(new SimpleContainer(stack), level.registryAccess());

                // [여기서부터가 내가 고쳐준 부분이야♡]
                if (stack.getCount() == 1) {
                    // 경우 1: 아이템이 딱 1개 남았을 때 -> 결과물로 바꿔치기!
                    // setStackInSlot을 쓰면 onContentsChanged가 호출돼서 알아서 저장되고 패킷도 보내짐.
                    entity.itemHandler.setStackInSlot(0, result.copy());

                    // 진행도 초기화 (더 이상 구울 게 없거나, 이미 결과물이 되었으니)
                    entity.resetProgress();
                    entity.totalTicks = 0;
                } else {
                    // 경우 2: 아이템이 여러 개 겹쳐있을 때 -> 1개 줄이고 결과물은 뱉기!

                    // 입력 아이템 1개 감소 (이건 내부 객체만 건드리는 거라 onContentsChanged 호출 안 됨!)
                    stack.shrink(1);

                    // 결과물 바닥에 떨구기
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, result.copy());

                    // 계속 구워야 하니까 진행도만 0으로. totalTicks는 유지해도 됨.
                    entity.resetProgress();

                    // [중요!] stack.shrink()는 자동 저장이 안 돼.
                    // 강제로 "야! 아이템 줄어들었어! 저장해!" 하고 소리쳐야 해.
                    entity.setChanged();
                    // 클라이언트한테도 알려줘서 화면 갱신 (네가 만든 핸들러 로직 활용)
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        } else {
            // 레시피 없으면 초기화 (이상한 거 넣었을 때)
            entity.resetProgress();
            entity.totalTicks = 0;
        }
    }

    // 진행도 0으로 만드는 도우미 메서드
    private void resetProgress() {
        this.progress = 0;
    }

    // 외부에서(파이프 등) 아이템 넣고 뺄 때 필요한 기능이야. 이거 없으면 깡통임.
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    // 청소
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    // 아이템 핸들러 가져오는 도우미 메서드 (나중에 블록에서 쓸 거임)
    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }
}
