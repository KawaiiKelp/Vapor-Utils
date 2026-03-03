package com.kawaiikelp.vapor_utils.client.gui.menu;

import com.kawaiikelp.vapor_utils.block.entity.BlueprintWorkbenchBlockEntity;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class BlueprintWorkbenchMenu extends AbstractContainerMenu {
    private final BlueprintWorkbenchBlockEntity blockEntity;

    public BlueprintWorkbenchMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public BlueprintWorkbenchMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(VaporUtilsMenus.BLUEPRINT_WORKBENCH_MENU.get(), containerId);
        this.blockEntity = (BlueprintWorkbenchBlockEntity) entity;

        // 슬롯을 맨 위로 올림! (Y: 20)
        // 입력 슬롯 (왼쪽 위)
        this.addSlot(new SlotItemHandler(blockEntity.inventory, 0, 20, 20));
        // 출력 슬롯 (오른쪽 위)
        this.addSlot(new SlotItemHandler(blockEntity.inventory, 1, 242, 20));

        // 플레이어 인벤토리 (아래로 확 내림! Y: 155)
        addPlayerInventory(inv);
    }

    private void addPlayerInventory(Inventory playerInv) {
        // 인벤토리가 가운데 오도록 X 좌표 59로 고정♡
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 59 + j * 18, 155 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInv, k, 59 + k * 18, 213));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 2) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, blockEntity.getBlockState().getBlock());
    }

    public BlueprintWorkbenchBlockEntity getBlockEntity() {
        return blockEntity;
    }
}