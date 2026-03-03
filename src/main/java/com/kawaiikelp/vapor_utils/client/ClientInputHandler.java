package com.kawaiikelp.vapor_utils.client;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.item.ProjectorItem;
import com.kawaiikelp.vapor_utils.network.RotateProjectorPacket;
import com.kawaiikelp.vapor_utils.network.VaporUtilsPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VaporUtils.MODID, value = Dist.CLIENT)
public class ClientInputHandler {
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isCrouching()) {
            if (mc.player.getMainHandItem().getItem() instanceof ProjectorItem) {
                // 스크롤이 발생하면 패킷 전송!
                boolean direction = event.getScrollDelta() > 0;
                VaporUtilsPacketHandler.INSTANCE.sendToServer(new RotateProjectorPacket(direction));
                // 마우스 원래 기능(아이템 슬롯 교체) 막기!
                event.setCanceled(true);
            }
        }
    }
}