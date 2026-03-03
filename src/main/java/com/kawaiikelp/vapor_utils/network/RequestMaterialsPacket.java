package com.kawaiikelp.vapor_utils.network;

import com.kawaiikelp.vapor_utils.block.entity.BlueprintWorkbenchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestMaterialsPacket {
    private final BlockPos pos;
    private final String fileName;

    public RequestMaterialsPacket(BlockPos pos, String fileName) {
        this.pos = pos;
        this.fileName = fileName;
    }

    public static void encode(RequestMaterialsPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.fileName);
    }

    public static RequestMaterialsPacket decode(FriendlyByteBuf buf) {
        return new RequestMaterialsPacket(buf.readBlockPos(), buf.readUtf());
    }

    public static void handle(RequestMaterialsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player != null && player.level().getBlockEntity(msg.pos) instanceof BlueprintWorkbenchBlockEntity workbench) {
                // 서버에서 재료를 계산해서 다시 플레이어한테 쏴줄 거야!
                workbench.sendMaterialsToPlayer(player, msg.fileName);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}