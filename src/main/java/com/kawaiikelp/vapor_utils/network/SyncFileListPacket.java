package com.kawaiikelp.vapor_utils.network;

import com.kawaiikelp.vapor_utils.client.gui.screen.BlueprintWorkbenchScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncFileListPacket {
    private final List<String> files;

    public SyncFileListPacket(List<String> files) {
        this.files = files;
    }

    public static void encode(SyncFileListPacket msg, FriendlyByteBuf buf) {
        buf.writeCollection(msg.files, FriendlyByteBuf::writeUtf);
    }

    public static SyncFileListPacket decode(FriendlyByteBuf buf) {
        return new SyncFileListPacket(buf.readCollection(ArrayList::new, FriendlyByteBuf::readUtf));
    }

    public static void handle(SyncFileListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 클라이언트에서 현재 열려있는 화면이 작업대라면 목록을 업데이트해!
            if (Minecraft.getInstance().screen instanceof BlueprintWorkbenchScreen screen) {
                screen.updateFileList(msg.files);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}