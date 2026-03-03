package com.kawaiikelp.vapor_utils.network;

import com.kawaiikelp.vapor_utils.client.gui.screen.BlueprintWorkbenchScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncMaterialsPacket {
    private final Map<String, Integer> materials;

    public SyncMaterialsPacket(Map<String, Integer> materials) {
        this.materials = materials;
    }

    public static void encode(SyncMaterialsPacket msg, FriendlyByteBuf buf) {
        buf.writeMap(msg.materials, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeInt);
    }

    public static SyncMaterialsPacket decode(FriendlyByteBuf buf) {
        return new SyncMaterialsPacket(buf.readMap(HashMap::new, FriendlyByteBuf::readUtf, FriendlyByteBuf::readInt));
    }

    public static void handle(SyncMaterialsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof BlueprintWorkbenchScreen screen) {
                screen.receiveMaterials(msg.materials);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}