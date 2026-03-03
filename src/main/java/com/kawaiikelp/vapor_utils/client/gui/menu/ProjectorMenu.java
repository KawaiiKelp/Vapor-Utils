package com.kawaiikelp.vapor_utils.client.gui.menu;

import com.kawaiikelp.vapor_utils.item.BlueprintItem;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsMenus; // 곧 만들 거야
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ProjectorMenu extends AbstractContainerMenu {

    // 투영기 아이템 그 자체 (여기에 데이터를 저장할 거야)
    private final ItemStack projectorStack;

    // 투영기 안에 들어갈 가상의 인벤토리 (1칸짜리)
    private final ItemStackHandler internalInventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            // 청사진(Blueprint)만 들어가게 필터링!
            return stack.getItem() instanceof BlueprintItem;
        }

        @Override
        protected void onContentsChanged(int slot) {
            // 내용물이 바뀌면 투영기 아이템 NBT에 즉시 저장!
            // "inventory"라는 태그 안에 저장할 거야.
            projectorStack.getOrCreateTag().put("inventory", this.serializeNBT());
        }
    };

    // 클라이언트용 생성자 (패킷 받아서 열릴 때)
    public ProjectorMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, playerInv.player.getMainHandItem());
    }

    // 서버용 생성자 (실제 열릴 때)
    public ProjectorMenu(int containerId, Inventory playerInv, ItemStack projectorStack) {
        super(VaporUtilsMenus.PROJECTOR_MENU.get(), containerId);
        this.projectorStack = projectorStack;

        if (projectorStack.hasTag() && projectorStack.getTag().contains("inventory")) {
            internalInventory.deserializeNBT(projectorStack.getTag().getCompound("inventory"));
        }

        // [변경] 슬롯 위치 이동!
        // 그림 보니까 대충 x=10, y=20 정도? (텍스처에 맞춰서 숫자 조절해!)
        this.addSlot(new SlotItemHandler(internalInventory, 0, 8, 16));

        // 플레이어 인벤토리는 하단에 (좌표 조절 필요할 수도 있음)
        addPlayerInventory(playerInv, 8, 102); // y좌표를 좀 내렸어. 미리보기 화면 때문에.
    }

    // 인벤토리 추가 메서드 (좌표 인자 받게 수정)
    private void addPlayerInventory(Inventory playerInv, int x, int y) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, x + j * 18, y + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInv, k, x + k * 18, y + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        // 투영기를 손에 들고 있어야만 메뉴가 유지됨!
        return player.getMainHandItem() == projectorStack;
    }

    // Shift+클릭 기능 (이거 없으면 불편해서 욕먹어)
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 1) { // 투영기 슬롯 -> 플레이어 인벤
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) { // 플레이어 인벤 -> 투영기 슬롯
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    public ItemStack getProjectorStack() {
        return this.projectorStack;
    }
}