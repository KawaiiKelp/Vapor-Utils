package com.kawaiikelp.vapor_utils.client.renderer;

import com.kawaiikelp.vapor_utils.item.ProjectorItem;
import com.kawaiikelp.vapor_utils.network.ToggleProjectionPacket;
import com.kawaiikelp.vapor_utils.network.VaporUtilsPacketHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchematicRenderer {

    private static class RenderCache {
        final StructureTemplate template;
        final List<BlockEntity> blockEntities = new ArrayList<>();

        RenderCache(StructureTemplate template, Level level) {
            this.template = template;
            for (StructureTemplate.StructureBlockInfo info : template.palettes.get(0).blocks()) {
                if (info.nbt() != null) {
                    BlockEntity be = BlockEntity.loadStatic(info.pos(), info.state(), info.nbt());
                    if (be != null) {
                        be.setLevel(level);
                        blockEntities.add(be);
                    }
                }
            }
        }
    }

    private static final Map<String, RenderCache> CACHE = new HashMap<>();

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // [핵심] 인벤토리 전체에서 켜져 있는 투영기 찾기♡
        ItemStack projectorStack = findActiveProjector(mc.player);
        if (projectorStack.isEmpty()) return;

        CompoundTag tag = projectorStack.getTag();
        if (tag == null || !tag.contains("inventory") || !tag.contains("anchor")) return;

        // NBT 파싱
        CompoundTag invTag = tag.getCompound("inventory");
        if (!invTag.contains("Items")) return;
        CompoundTag itemTag = invTag.getList("Items", 10).getCompound(0);
        if (!itemTag.contains("tag")) return;

        CompoundTag blueprintTag = itemTag.getCompound("tag");
        String fileName = blueprintTag.getString("schematic_file");
        BlockPos anchor = NbtUtils.readBlockPos(tag.getCompound("anchor"));
        int rotIndex = tag.getInt("rotation");

        RenderCache renderCache = getRenderCache(fileName, mc.level);
        if (renderCache == null) return;

        PoseStack poseStack = event.getPoseStack();
        BlockRenderDispatcher blockDispatcher = mc.getBlockRenderer();
        BlockEntityRenderDispatcher beDispatcher = mc.getBlockEntityRenderDispatcher();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        Vec3 camPos = event.getCamera().getPosition();

        int ghostBlockCount = 0; // 아직 안 지어진 블록 개수♡

        poseStack.pushPose();
        poseStack.translate(anchor.getX() - camPos.x, anchor.getY() - camPos.y, anchor.getZ() - camPos.z);

        if (rotIndex != 0) {
            poseStack.mulPose(Axis.YP.rotationDegrees(rotIndex * -90));
            switch (rotIndex) {
                case 1 -> poseStack.translate(0, 0, -1);
                case 2 -> poseStack.translate(-1, 0, -1);
                case 3 -> poseStack.translate(-1, 0, 0);
            }
        }

        Rotation mcRotation = getRotation(rotIndex);

        // 1. 일반 블록 렌더링
        for (StructureTemplate.StructureBlockInfo info : renderCache.template.palettes.get(0).blocks()) {
            BlockPos worldPos = anchor.offset(rotatePos(info.pos(), rotIndex));
            BlockState targetState = info.state().rotate(mcRotation);
            if (mc.level.getBlockState(worldPos).getBlock() == targetState.getBlock()) continue;
            if (targetState.isAir()) continue;

            ghostBlockCount++; // 지어야 할 블록 발견♡

            if (targetState.getRenderShape() == RenderShape.MODEL) {
                poseStack.pushPose();
                poseStack.translate(info.pos().getX(), info.pos().getY(), info.pos().getZ());
                blockDispatcher.renderSingleBlock(targetState, poseStack, buffer, 15728880, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.translucent());
                poseStack.popPose();
            }
        }

        // 2. 블록 엔티티 렌더링
        for (BlockEntity be : renderCache.blockEntities) {
            BlockPos worldPos = anchor.offset(rotatePos(be.getBlockPos(), rotIndex));
            if (mc.level.getBlockState(worldPos).getBlock() == be.getBlockState().getBlock()) continue;

            ghostBlockCount++; // 얘도 지어야 해♡

            poseStack.pushPose();
            poseStack.translate(be.getBlockPos().getX(), be.getBlockPos().getY(), be.getBlockPos().getZ());
            try {
                var renderer = beDispatcher.getRenderer(be);
                if (renderer != null) renderer.render(be, event.getPartialTick(), poseStack, buffer, 15728880, OverlayTexture.NO_OVERLAY);
            } catch (Exception ignored) {}
            poseStack.popPose();
        }

        poseStack.popPose();

        // [핵심] 다 지었으면 서버에 패킷 보내서 끄기♡
        if (ghostBlockCount == 0 && mc.player.tickCount % 20 == 0) {
            VaporUtilsPacketHandler.INSTANCE.sendToServer(new ToggleProjectionPacket());
        }
    }

    private static ItemStack findActiveProjector(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.getItem() instanceof ProjectorItem && s.hasTag() && s.getTag().getBoolean("active")) return s;
        }
        return ItemStack.EMPTY;
    }

    private static BlockPos rotatePos(BlockPos pos, int rotation) {
        return switch (rotation) {
            case 1 -> new BlockPos(-pos.getZ(), pos.getY(), pos.getX());
            case 2 -> new BlockPos(-pos.getX(), pos.getY(), -pos.getZ());
            case 3 -> new BlockPos(pos.getZ(), pos.getY(), -pos.getX());
            default -> pos;
        };
    }

    private static Rotation getRotation(int index) {
        return switch (index) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    private static RenderCache getRenderCache(String fileName, Level level) {
        if (CACHE.containsKey(fileName)) return CACHE.get(fileName);
        File file = new File(Minecraft.getInstance().gameDirectory, "schematics/vapor_utils/" + fileName);
        if (!file.exists()) return null;
        try (FileInputStream stream = new FileInputStream(file)) {
            CompoundTag nbt = NbtIo.readCompressed(stream);
            StructureTemplate template = new StructureTemplate();
            template.load(Minecraft.getInstance().level.holderLookup(net.minecraft.core.registries.Registries.BLOCK), nbt);
            RenderCache cache = new RenderCache(template, level);
            CACHE.put(fileName, cache);
            return cache;
        } catch (Exception e) { return null; }
    }
}