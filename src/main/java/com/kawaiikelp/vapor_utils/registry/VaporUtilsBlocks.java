package com.kawaiikelp.vapor_utils.registry;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.block.BlueprintWorkbenchBlock;
import com.kawaiikelp.vapor_utils.block.DryingRackBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class VaporUtilsBlocks {
    // 블록을 등록할 지연 레지스터(DeferredRegister) 생성
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, VaporUtils.MODID);

    // [변경] 단일 블록 대신 Map으로 관리!
    // "oak" -> 블록객체, "spruce" -> 블록객체 ... 이렇게 저장될 거야.
    public static final Map<String, RegistryObject<Block>> DRYING_RACKS = new HashMap<>();

    // 나무 종류 리스트 (아저씨가 원하는 거 다 적어)
    private static final String[] WOOD_TYPES = {
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "bamboo"
    };

    static {
        // 반복문(Loop) 돌려서 공장처럼 찍어내기! 똑똑하지?♡
        for (String wood : WOOD_TYPES) {
            String name = wood + "_drying_rack"; // 예: oak_drying_rack

            DRYING_RACKS.put(wood, BLOCKS.register(name,
                    () -> new DryingRackBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.WOOD)
                                    .strength(2.0f)
                                    .noOcclusion() // 투명한 부분 있으니까 필수
                    )
            ));
        }
    }

    public static final RegistryObject<Block> BLUEPRINT_WORKBENCH = BLOCKS.register("blueprint_workbench",
            () -> new BlueprintWorkbenchBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f).noOcclusion()));

    // 혹시 옛날 코드에서 DRYING_RACK 하나만 찾던 거 있을까 봐 남겨두는 호환용 (참나무로 퉁침)
    // 나중에 싹 찾아서 지우는 게 좋긴 해.
    public static RegistryObject<Block> getDefault() {
        return DRYING_RACKS.get("oak");
    }
}
