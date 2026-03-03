package com.kawaiikelp.vapor_utils.network;

import com.kawaiikelp.vapor_utils.block.entity.BlueprintWorkbenchBlockEntity;
import com.kawaiikelp.vapor_utils.item.BlueprintMapperItem;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CreateBlueprintPacket {
    private final BlockPos pos;
    private final String name;

    public CreateBlueprintPacket(BlockPos pos, String name) {
        this.pos = pos;
        this.name = name;
    }

    public static void encode(CreateBlueprintPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.name);
    }

    public static CreateBlueprintPacket decode(FriendlyByteBuf buf) {
        return new CreateBlueprintPacket(buf.readBlockPos(), buf.readUtf());
    }

    public static void handle(CreateBlueprintPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (player.level().getBlockEntity(msg.pos) instanceof BlueprintWorkbenchBlockEntity workbench) {
                ItemStack input = workbench.inventory.getStackInSlot(0);
                ItemStack output = workbench.inventory.getStackInSlot(1);

                // 출력 슬롯이 비어있어야만 인쇄 가능!
                if (!output.isEmpty()) return;

                // 케이스 1: 기록기(Mapper)가 들어있을 때 -> 새 파일 저장 및 출력
                if (input.getItem() instanceof BlueprintMapperItem && input.hasTag() && input.getTag().contains("pos2")) {
                    BlueprintMapperItem.saveSchematicToWorkbench(player.level(), player, input, workbench, msg.name);
                }
                // 케이스 2: 종이(Paper)나 빈 청사진이 들어있고, 리스트에서 파일을 선택했을 때 -> 복사본 출력
                else if ((input.getItem() == Items.PAPER || input.getItem() == VaporUtilsItems.BLUEPRINT.get()) && !msg.name.isEmpty()) {
                    ItemStack blueprintStack = new ItemStack(VaporUtilsItems.BLUEPRINT.get());
                    CompoundTag tag = blueprintStack.getOrCreateTag();

                    // msg.name에는 GUI에서 선택된 파일 이름이 넘어옴!
                    String fileName = msg.name.endsWith(".nbt") ? msg.name : msg.name + ".nbt";
                    tag.putString("schematic_file", fileName);

                    workbench.inventory.setStackInSlot(1, blueprintStack);
                    input.shrink(1); // 종이 1장 소모♡
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}