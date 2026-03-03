package com.kawaiikelp.vapor_utils.network;

import com.kawaiikelp.vapor_utils.VaporUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class VaporUtilsPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(VaporUtils.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    // VaporUtilsPacketHandler.java 안에 있는 register() 메서드

    public static void register() {
        int id = 0;

        // SaveSchematicPacket 등록 (기존)
        // INSTANCE.registerMessage(id++, SaveSchematicPacket.class, SaveSchematicPacket::encode, SaveSchematicPacket::decode, SaveSchematicPacket::handle);

        // [여기!] ToggleProjectionPacket 등록
        // ::decode 라고 정확히 써! (아까처럼 decode2 이런 거 쓰지 말고!)
        INSTANCE.registerMessage(id++, ToggleProjectionPacket.class, ToggleProjectionPacket::encode, ToggleProjectionPacket::decode, ToggleProjectionPacket::handle);

        INSTANCE.registerMessage(id++, RotateProjectorPacket.class, RotateProjectorPacket::encode, RotateProjectorPacket::decode, RotateProjectorPacket::handle);

        INSTANCE.registerMessage(id++, CreateBlueprintPacket.class, CreateBlueprintPacket::encode, CreateBlueprintPacket::decode, CreateBlueprintPacket::handle);

        INSTANCE.registerMessage(id++, SyncFileListPacket.class, SyncFileListPacket::encode, SyncFileListPacket::decode, SyncFileListPacket::handle);

        INSTANCE.registerMessage(id++, RequestMaterialsPacket.class, RequestMaterialsPacket::encode, RequestMaterialsPacket::decode, RequestMaterialsPacket::handle);

        INSTANCE.registerMessage(id++, SyncMaterialsPacket.class, SyncMaterialsPacket::encode, SyncMaterialsPacket::decode, SyncMaterialsPacket::handle);
    }
}