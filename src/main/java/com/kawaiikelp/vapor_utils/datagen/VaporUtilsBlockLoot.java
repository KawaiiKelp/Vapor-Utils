package com.kawaiikelp.vapor_utils.datagen;

import com.kawaiikelp.vapor_utils.registry.VaporUtilsBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import java.util.Set;

public class VaporUtilsBlockLoot extends BlockLootSubProvider {
    public VaporUtilsBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        // 모든 건조대는 자기 자신을 드랍한다.
        VaporUtilsBlocks.DRYING_RACKS.values().forEach(block -> {
            dropSelf(block.get());
        });
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        // 이거 안 하면 "너 왜 등록 안 된 블록 있어?" 하고 에러 남.
        return VaporUtilsBlocks.DRYING_RACKS.values().stream().map(RegistryObject::get)::iterator;
    }
}