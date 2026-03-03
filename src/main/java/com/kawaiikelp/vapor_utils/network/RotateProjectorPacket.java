package com.kawaiikelp.vapor_utils.network;

import com.kawaiikelp.vapor_utils.item.ProjectorItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RotateProjectorPacket {
    private final boolean direction; // true면 오른쪽, false면 왼쪽

    public RotateProjectorPacket(boolean direction) {
        this.direction = direction;
    }

    public static void encode(RotateProjectorPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.direction);
    }

    public static RotateProjectorPacket decode(FriendlyByteBuf buf) {
        return new RotateProjectorPacket(buf.readBoolean());
    }

    public static void handle(RotateProjectorPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof ProjectorItem) {
                var tag = stack.getOrCreateTag();
                // 0, 1, 2, 3 (0도, 90도, 180도, 270도)
                int rot = tag.getInt("rotation");
                if (msg.direction) rot = (rot + 1) % 4;
                else rot = (rot + 3) % 4;
                tag.putInt("rotation", rot);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}