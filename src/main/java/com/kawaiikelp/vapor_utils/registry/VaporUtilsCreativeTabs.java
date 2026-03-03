package com.kawaiikelp.vapor_utils.registry; // 패키지명 확인해!

import com.kawaiikelp.vapor_utils.VaporUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class VaporUtilsCreativeTabs {

    // 레지스터 생성 (여기에 탭을 등록할 거야)
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VaporUtils.MODID);

    // 탭 만들기!
    public static final RegistryObject<CreativeModeTab> VAPOR_UTILS_TAB = CREATIVE_MODE_TABS.register("vapor_utils_tab",
            () -> CreativeModeTab.builder()
                    // 1. 아이콘 설정: 아까 그 '소나무 건조대'로 해줄게. (다른 걸로 바꾸려면 여기서 바꿔)
                    // .get() 뒤에 .get() 또 붙여야 아이템이 나와 (RegistryObject -> Block -> Item)
                    .icon(() -> new ItemStack(VaporUtilsBlocks.DRYING_RACKS.get("spruce").get()))

                    // 2. 탭 이름 설정 (lang 파일 키값)
                    .title(Component.translatable("itemGroup.vapor_utils"))

                    // 3. 아이템 채워넣기
                    .displayItems((itemDisplayParameters, output) -> {

                        // 여기에 아저씨가 만든 아이템들 하나씩 추가하면 돼.
                        // 예시: 맵에 있는 건조대 다 꺼내서 등록하기
                        VaporUtilsBlocks.DRYING_RACKS.forEach((name, block) -> {
                            output.accept(block.get());
                        });

                        output.accept(VaporUtilsBlocks.BLUEPRINT_WORKBENCH.get());

                        // 다른 아이템도 있으면 추가해
                        output.accept(VaporUtilsItems.HEALING_AXE.get());
                        output.accept(VaporUtilsItems.BLUEPRINT_MAPPER.get());
                        output.accept(VaporUtilsItems.PROJECTOR.get());
                    })
                    .build());

    // 메인 클래스에서 이거 호출해야 해!
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}