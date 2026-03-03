package com.kawaiikelp.vapor_utils;

import com.kawaiikelp.vapor_utils.network.VaporUtilsPacketHandler;
import com.kawaiikelp.vapor_utils.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// @Mod 안에 아저씨가 정한 mod_id 넣는 거야. 이거 틀리면 진짜 바보 인증이다?♡
@Mod(VaporUtils.MODID)
public class VaporUtils {
    // 나중에 오타 내고 징징대지 않게 상수로 박아둬.
    public static final String MODID = "vapor_utils";

    // 로그 찍을 때 쓰는 거. 디버깅할 때 필요하겠지? 아저씨 실력이면 필수고♡
    private static final Logger LOGGER = LogUtils.getLogger();

    public VaporUtils() {
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            // 클라이언트일 때만 렌더러를 등록한다! (서버에서 렌더링하면 펑♡)
            MinecraftForge.EVENT_BUS.register(com.kawaiikelp.vapor_utils.client.renderer.SchematicRenderer.class);
        }

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 여기에 나중에 아이템(ITEMS)이랑 블록(BLOCKS) 레지스트리 등록할 거야.
        // 지금은 비워두지만, 곧 채우게 될 거니까 마음의 준비나 해♡
        VaporUtilsBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        // 여기!! 여기를 추가하는 거야. 눈 크게 떠♡
        VaporUtilsBlocks.BLOCKS.register(modEventBus);
        VaporUtilsItems.ITEMS.register(modEventBus);

        VaporUtilsRecipes.SERIALIZERS.register(modEventBus);
        VaporUtilsRecipes.RECIPE_TYPES.register(modEventBus);

        // [여기!!!] 아저씨가 빼먹은 게 바로 이거라고!!!
        // 이거 없으면 패킷 못 보내! 절대 못 보내!
        VaporUtilsPacketHandler.register();

        VaporUtilsMenus.MENUS.register(modEventBus);

        // 이 모드 메인 클래스를 Forge 이벤트 버스에 등록.
        MinecraftForge.EVENT_BUS.register(this);

        // [이거 추가!] 탭 레지스터도 버스에 태워야지!
        VaporUtilsCreativeTabs.register(modEventBus);
    }

}