package com.kawaiikelp.vapor_utils.datagen;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class VaporUtilsBlockTagProvider extends BlockTagsProvider {
    public VaporUtilsBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, VaporUtils.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        // 모든 건조대에 "도끼로 캐는 거임" 태그 붙이기
        var tagBuilder = tag(BlockTags.MINEABLE_WITH_AXE);

        VaporUtilsBlocks.DRYING_RACKS.values().forEach(block -> {
            tagBuilder.add(block.get());
        });
    }
}