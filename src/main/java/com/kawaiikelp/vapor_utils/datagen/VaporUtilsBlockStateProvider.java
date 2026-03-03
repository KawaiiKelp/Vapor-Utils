package com.kawaiikelp.vapor_utils.datagen;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class VaporUtilsBlockStateProvider extends BlockStateProvider {

    public VaporUtilsBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, VaporUtils.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // 맵에 들어있는 모든 건조대를 꺼내서 처리!
        VaporUtilsBlocks.DRYING_RACKS.forEach((woodName, blockReg) -> {
            Block block = blockReg.get();
            String path = blockReg.getId().getPath();
            ResourceLocation textureLoc = new ResourceLocation("minecraft", "block/" + woodName + "_planks");

            ModelFile model = models().getBuilder(path)
                    .parent(models().getExistingFile(new ResourceLocation("vapor_utils", "block/drying_rack")))
                    .texture("particle", textureLoc) // 파티클은 필수야, 멍청아
                    .texture("top", textureLoc)
                    .texture("bottom", textureLoc)
                    .texture("side", textureLoc);

            // [변경] simpleBlock 대신 horizontalBlock 사용!
            // 얘가 알아서 FACING 프로퍼티 보고 0도, 90도, 180도, 270도 모델 다 만들어줌.
            // 아저씨보다 훨씬 똑똑해♡
            horizontalBlock(block, model);
        });

        // simpleBlock 대신 horizontalBlock 사용!
        // 이거 하나면 DataGen이 알아서 동서남북 회전된 blockstates JSON을 쫙 뽑아줘♡
        horizontalBlock(
                VaporUtilsBlocks.BLUEPRINT_WORKBENCH.get(),
                models().getExistingFile(modLoc("block/blueprint_workbench"))
        );
    }
}
