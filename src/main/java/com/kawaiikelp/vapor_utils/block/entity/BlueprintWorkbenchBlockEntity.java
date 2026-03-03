package com.kawaiikelp.vapor_utils.block.entity;

import com.kawaiikelp.vapor_utils.registry.VaporUtilsBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlueprintWorkbenchBlockEntity extends BlockEntity {
    // 슬롯 0: 입력 (Mapper나 종이), 슬롯 1: 출력 (Blueprint)
    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public BlueprintWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(VaporUtilsBlockEntities.BLUEPRINT_WORKBENCH_BE.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("inventory"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", inventory.serializeNBT());
        super.saveAdditional(tag);
    }

    public void syncFilesToPlayer(net.minecraft.server.level.ServerPlayer player) {
        java.io.File dir = new java.io.File(level.getServer().getServerDirectory(), "schematics/vapor_utils");
        List<String> fileNames = new ArrayList<>();

        if (dir.exists() && dir.isDirectory()) {
            java.io.File[] files = dir.listFiles((d, name) -> name.endsWith(".nbt"));
            if (files != null) {
                for (java.io.File f : files) {
                    fileNames.add(f.getName());
                }
            }
        }

        // 패킷 발송!
        com.kawaiikelp.vapor_utils.network.VaporUtilsPacketHandler.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new com.kawaiikelp.vapor_utils.network.SyncFileListPacket(fileNames)
        );
    }

    // BlueprintWorkbenchBlockEntity.java 내부에 추가♡

    public void sendMaterialsToPlayer(net.minecraft.server.level.ServerPlayer player, String fileName) {
        java.io.File file = new java.io.File(level.getServer().getServerDirectory(), "schematics/vapor_utils/" + fileName);
        if (!file.exists()) return;

        try (java.io.FileInputStream stream = new java.io.FileInputStream(file)) {
            net.minecraft.nbt.CompoundTag nbt = net.minecraft.nbt.NbtIo.readCompressed(stream);
            net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate template = new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate();
            template.load(level.holderLookup(net.minecraft.core.registries.Registries.BLOCK), nbt);

            Map<String, Integer> counts = new HashMap<>();
            // 템플릿 안의 모든 블록을 순회하며 개수 세기!
            for (var info : template.palettes.get(0).blocks()) {
                if (info.state().isAir()) continue;
                // 블록 이름을 키값으로 저장 (예: "minecraft:stone" -> 64)
                String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(info.state().getBlock()).toString();
                counts.put(blockId, counts.getOrDefault(blockId, 0) + 1);
            }

            // 결과 전송!
            com.kawaiikelp.vapor_utils.network.VaporUtilsPacketHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new com.kawaiikelp.vapor_utils.network.SyncMaterialsPacket(counts)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}