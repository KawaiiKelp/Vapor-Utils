package com.kawaiikelp.vapor_utils.network;

import com.kawaiikelp.vapor_utils.item.ProjectorItem;
import com.kawaiikelp.vapor_utils.client.gui.menu.ProjectorMenu; // 추가
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ToggleProjectionPacket {
    public ToggleProjectionPacket() {}

    public static void encode(ToggleProjectionPacket msg, FriendlyByteBuf buf) {}

    public static ToggleProjectionPacket decode(FriendlyByteBuf buf) {
        return new ToggleProjectionPacket();
    }

    public static void handle(ToggleProjectionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // [1] 우선 플레이어가 투영기 GUI(Menu)를 열고 있는지 확인해!
            if (player.containerMenu instanceof ProjectorMenu menu) {
                ItemStack stack = menu.getProjectorStack();
                if (!stack.isEmpty() && stack.getItem() instanceof ProjectorItem) {
                    var tag = stack.getOrCreateTag();
                    boolean active = !tag.getBoolean("active"); // 상태 반전 (토글)

                    // 켤 때는 다른 투영기 끄기 (아저씨 컴 터지지 말라고♡)
                    if (active) {
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            ItemStack s = player.getInventory().getItem(i);
                            if (s.getItem() instanceof ProjectorItem) s.getOrCreateTag().putBoolean("active", false);
                        }
                    }

                    tag.putBoolean("active", active);

                    // 메시지 출력
                    player.sendSystemMessage(Component.translatable(active ? "message.vapor_utils.projector.on" : "message.vapor_utils.projector.off"));
                    return; // GUI에서 처리했으면 여기서 종료!
                }
            }

            // [2] GUI를 안 열고 있다면? (SchematicRenderer에서 보낸 자동 종료 신호!)
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack s = player.getInventory().getItem(i);
                if (s.getItem() instanceof ProjectorItem && s.hasTag() && s.getTag().getBoolean("active")) {
                    s.getOrCreateTag().putBoolean("active", false);
                    player.sendSystemMessage(Component.translatable("message.vapor_utils.projector.complete"));
                    break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}