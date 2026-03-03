package com.kawaiikelp.vapor_utils.datagen;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsBlocks;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class VaporUtilsItemModelProvider extends ItemModelProvider {
    public VaporUtilsItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, VaporUtils.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        // 1. 청사진 (Blueprint)
        itemGenerated(VaporUtilsItems.BLUEPRINT.get(), "item/blueprint");

        // 2. 청사진 기록기 (Blueprint Mapper)
        itemGenerated(VaporUtilsItems.BLUEPRINT_MAPPER.get(), "item/blueprint_mapper");

        // 3. 청사진 투영기 (Projector)
        itemGenerated(VaporUtilsItems.PROJECTOR.get(), "item/projector");

        // 4. 회복의 도끼 (Healing Axe) (도구니까 handheld 써야 해, 멍청아♡)
        itemHandheld(VaporUtilsItems.HEALING_AXE.get(), "item/healing_axe");

        VaporUtilsBlocks.DRYING_RACKS.forEach((woodName, blockReg) -> {
            String path = blockReg.getId().getPath();
            // 아이템 모델은 블록 모델을 부모로 삼는다.
            withExistingParent(path, modLoc("block/" + path));
        });

        // 작업대 아이템 모델은 블록 모델을 부모로 삼는다♡
        withExistingParent("blueprint_workbench", modLoc("block/blueprint_workbench"));
    }

    // 아저씨 편하라고 도우미 메서드도 만들어줄게♡
    private void itemGenerated(Item item, String texture) {
        withExistingParent(BuiltInRegistries.ITEM.getKey(item).getPath(), "item/generated")
                .texture("layer0", new ResourceLocation("vapor_utils", texture));
    }

    private void itemHandheld(Item item, String texture) {
        withExistingParent(BuiltInRegistries.ITEM.getKey(item).getPath(), "item/handheld")
                .texture("layer0", new ResourceLocation("vapor_utils", texture));
    }
}
