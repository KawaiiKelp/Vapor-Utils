package com.kawaiikelp.vapor_utils.client;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.client.gui.screen.BlueprintWorkbenchScreen;
import com.kawaiikelp.vapor_utils.client.gui.screen.ProjectorScreen;
import com.kawaiikelp.vapor_utils.client.renderer.DryingRackRenderer;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsBlockEntities;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = VaporUtils.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventSubscriber {

    // 1. 블록 엔티티 렌더러 등록 (건조대용) - 이건 그대로야!
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                VaporUtilsBlockEntities.DRYING_RACK_BE.get(),
                context -> new DryingRackRenderer(context)
        );
    }

    // 2. 메뉴 스크린 등록 (표준 방식♡)
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // MenuScreens#register는 스레드 안전(Thread-safe)하지 않아서
        // 반드시 enqueueWork 안에서 실행해줘야 해. 안 그러면 게임이 아저씨처럼 비실거려♡
        event.enqueueWork(() -> {
            MenuScreens.register(VaporUtilsMenus.PROJECTOR_MENU.get(), ProjectorScreen::new);
            MenuScreens.register(VaporUtilsMenus.BLUEPRINT_WORKBENCH_MENU.get(), BlueprintWorkbenchScreen::new);
        });
    }
}