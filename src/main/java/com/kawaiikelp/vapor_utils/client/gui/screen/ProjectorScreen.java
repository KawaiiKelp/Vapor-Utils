package com.kawaiikelp.vapor_utils.client.gui.screen;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.client.gui.menu.ProjectorMenu;
import com.kawaiikelp.vapor_utils.network.ToggleProjectionPacket;
import com.kawaiikelp.vapor_utils.network.VaporUtilsPacketHandler;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.joml.Quaternionf;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ProjectorScreen extends AbstractContainerScreen<ProjectorMenu> {
    // 아저씨가 그릴 텍스처 경로
    private static final ResourceLocation TEXTURE = new ResourceLocation(VaporUtils.MODID, "textures/gui/projector_gui.png");
    // [추가] 아저씨가 그려올 아이콘 경로!
    private static final ResourceLocation SLOT_ICON = new ResourceLocation(VaporUtils.MODID, "textures/gui/empty_blueprint_slot.png");

    // ProjectorScreen 클래스 안에 BE 캐시 추가
    private final List<BlockEntity> guiBeCache = new ArrayList<>();

    // 미리보기 창 좌표와 크기 (아저씨 그림 보고 대충 잡음, 조절해!)
    private static final int PREVIEW_X = 32;
    private static final int PREVIEW_Y = 15;
    private static final int PREVIEW_W = 112;
    private static final int PREVIEW_H = 72;

    private StructureTemplate cachedTemplate = null;
    private String lastFileName = "";

    // [추가] 조작용 변수들
    private float yaw = 45.0f;   // 좌우 회전
    private float pitch = 20.0f; // 상하 회전
    private float scale = 1.0f;  // 확대/축소 배율

    public ProjectorScreen(ProjectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;  // 텍스처 크기에 맞춰
        this.imageHeight = 186;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int rightPanelX = this.leftPos + 150;

        // 1. 투영 On/Off 버튼 (위쪽 화살표?)
        this.addRenderableWidget(Button.builder(Component.literal("P"), b -> {
            // 패킷 전송!
            VaporUtilsPacketHandler.INSTANCE.sendToServer(new ToggleProjectionPacket());
        }).bounds(rightPanelX, this.topPos + 20, 20, 20).tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal("Toggle Projection"))).build());

        // 2. 확대 버튼 (+)
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            scale += 0.2f;
            if (scale > 3.0f) scale = 3.0f;
        }).bounds(rightPanelX, this.topPos + 50, 20, 20).build());

        // 3. 축소 버튼 (-)
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            scale -= 0.2f;
            if (scale < 0.2f) scale = 0.2f;
        }).bounds(rightPanelX, this.topPos + 75, 20, 20).build());
    }

    // [추가] 마우스 드래그로 회전시키기!
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // 미리보기 영역 안에서만 작동하게 하려면 여기 조건문 추가하면 돼.
        // 일단은 화면 어디든 드래그하면 돌아가게 해줄게.
        this.yaw += (float) dragX;
        this.pitch += (float) dragY;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderPreview(graphics, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // [수정된 부분]
        // blit(텍스처, x, y, u, v, 그릴가로, 그릴세로, ★소스가로, ★소스세로)
        // 뒤에 176, 180을 꼭 적어줘야 파일 전체 크기를 인식해!
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        // [슬롯 힌트 아이콘]
        if (this.menu.getSlot(0).getItem().isEmpty()) {
            // 아까 ProjectorMenu에서 슬롯 위치를 (10, 20)으로 잡았지?
            // 그 좌표 그대로 쓰는 거야.
            int slotX = x + 8;
            int slotY = y + 16;

            // 힌트 아이콘도 마찬가지! 16x16짜리 파일이면 뒤에 16, 16을 적어줘야 해.
            graphics.blit(SLOT_ICON, slotX, slotY, 0, 0, 16, 16, 16, 16);
        }
    }

    private void renderPreview(GuiGraphics graphics, float partialTick) {
        ItemStack blueprint = this.menu.getSlot(0).getItem();

        // 1. 아이템 없으면 캐시 싹 비우기
        if (blueprint.isEmpty() || !blueprint.hasTag()) {
            cachedTemplate = null;
            lastFileName = "";
            guiBeCache.clear();
            return;
        }

        String fileName = blueprint.getTag().getString("schematic_file");

        // 2. 파일이 바뀌었을 때만 딱 한 번 로드해! (이게 진짜 캐시야, 바보야♡)
        if (!fileName.equals(lastFileName) || cachedTemplate == null) {
            cachedTemplate = loadTemplate(fileName);
            lastFileName = fileName;
            guiBeCache.clear();

            if (cachedTemplate != null) {
                for (StructureTemplate.StructureBlockInfo info : cachedTemplate.palettes.get(0).blocks()) {
                    if (info.nbt() != null) {
                        // 상대 위치 정보를 담아서 BE 로드
                        BlockEntity be = BlockEntity.loadStatic(info.pos(), info.state(), info.nbt());
                        if (be != null) {
                            be.setLevel(minecraft.level);
                            guiBeCache.add(be);
                        }
                    }
                }
            }
        }

        if (cachedTemplate == null) return;

        // --- 렌더링 시작 (Matrix 연산) ---
        graphics.enableScissor(this.leftPos + PREVIEW_X, this.topPos + PREVIEW_Y,
                this.leftPos + PREVIEW_X + PREVIEW_W, this.topPos + PREVIEW_Y + PREVIEW_H);

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        // 미리보기 창 중앙으로 이동
        poseStack.translate(this.leftPos + PREVIEW_X + PREVIEW_W / 2f,
                this.topPos + PREVIEW_Y + PREVIEW_H / 2f, 100);

        float maxDim = Math.max(cachedTemplate.getSize().getX(), Math.max(cachedTemplate.getSize().getY(), cachedTemplate.getSize().getZ()));
        float baseScale = 30f / Math.max(1, maxDim);
        float finalScale = baseScale * this.scale;

        poseStack.scale(finalScale, -finalScale, finalScale);
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));

        // 중심점 보정
        poseStack.translate(-cachedTemplate.getSize().getX() / 2f,
                -cachedTemplate.getSize().getY() / 2f,
                -cachedTemplate.getSize().getZ() / 2f);

        Lighting.setupFor3DItems();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        // 3. 일반 블록 그리기
        for (StructureTemplate.StructureBlockInfo info : cachedTemplate.palettes.get(0).blocks()) {
            if (info.state().getRenderShape() == RenderShape.MODEL) {
                poseStack.pushPose();
                poseStack.translate(info.pos().getX(), info.pos().getY(), info.pos().getZ());
                minecraft.getBlockRenderer().renderSingleBlock(info.state(), poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }
        }

        // 4. [수정] 캐시된 블록 엔티티 그리기 (이제 안 튕겨!♡)
        for (BlockEntity fakeBE : guiBeCache) {
            poseStack.pushPose();
            BlockPos pos = fakeBE.getBlockPos();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

            var renderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(fakeBE);
            if (renderer != null) {
                renderer.render(fakeBE, partialTick, poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
            }
            poseStack.popPose();
        }

        bufferSource.endBatch();
        Lighting.setupForFlatItems();
        poseStack.popPose();
        graphics.disableScissor();
    }

    // 템플릿 파일 읽어오기 (SchematicRenderer랑 비슷함)
    private StructureTemplate loadTemplate(String fileName) {
        try {
            File file = new File(Minecraft.getInstance().gameDirectory, "schematics/vapor_utils/" + fileName);
            if (file.exists()) {
                FileInputStream stream = new FileInputStream(file);
                CompoundTag nbt = NbtIo.readCompressed(stream);
                StructureTemplate t = new StructureTemplate();
                t.load(Minecraft.getInstance().level.holderLookup(net.minecraft.core.registries.Registries.BLOCK), nbt);
                stream.close();
                return t;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}