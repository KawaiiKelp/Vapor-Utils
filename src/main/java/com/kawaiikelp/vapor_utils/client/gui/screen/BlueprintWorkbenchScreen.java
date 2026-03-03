package com.kawaiikelp.vapor_utils.client.gui.screen;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.client.gui.menu.BlueprintWorkbenchMenu;
import com.kawaiikelp.vapor_utils.item.BlueprintItem;
import com.kawaiikelp.vapor_utils.network.CreateBlueprintPacket;
import com.kawaiikelp.vapor_utils.network.RequestMaterialsPacket;
import com.kawaiikelp.vapor_utils.network.VaporUtilsPacketHandler;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class BlueprintWorkbenchScreen extends AbstractContainerScreen<BlueprintWorkbenchMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(VaporUtils.MODID, "textures/gui/blueprint_workbench_gui.png");

    private List<String> fileList = new ArrayList<>();
    private Map<String, Integer> materialList = new LinkedHashMap<>();
    private String selectedFile = "";
    private StructureTemplate cachedTemplate = null;

    private int fileScrollOffset = 0;
    private float previewRotation = 0;
    private EditBox nameBox;

    public BlueprintWorkbenchScreen(BlueprintWorkbenchMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 280;
        this.imageHeight = 240;

        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 59;
        this.inventoryLabelY = 144;
    }

    @Override
    protected void init() {
        super.init();
        this.nameBox = new EditBox(this.font, leftPos + 45, topPos + 22, 125, 12, Component.literal(""));
        this.nameBox.setBordered(true);
        this.addRenderableWidget(this.nameBox);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.vapor_utils.btn_print"), b -> {
            // 빈칸이어도 무시되지 않게 확실한 로직!
            String outName = nameBox.getValue().trim();
            if (outName.isEmpty() && !selectedFile.isEmpty()) {
                outName = selectedFile.replace(".nbt", ""); // 리스트에서 선택한 파일 이름 쓰기
            }

            // 이름이 뭐라도 있으면 패킷 발사!
            if (!outName.isEmpty()) {
                VaporUtilsPacketHandler.INSTANCE.sendToServer(new CreateBlueprintPacket(menu.getBlockEntity().getBlockPos(), outName));
            }
        }).bounds(leftPos + 175, topPos + 18, 60, 20).build());
    }

    public void updateFileList(List<String> files) { this.fileList = files; }
    public void receiveMaterials(Map<String, Integer> materials) { this.materialList = materials; }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        checkSlotForPreview();

        renderFileList(graphics, mouseX, mouseY);
        renderPreview(graphics, partialTick);
        renderMaterials(graphics);

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void checkSlotForPreview() {
        ItemStack stack = this.menu.getSlot(1).hasItem() ? this.menu.getSlot(1).getItem() : this.menu.getSlot(0).getItem();
        if (stack.getItem() instanceof BlueprintItem && stack.hasTag() && stack.getTag().contains("schematic_file")) {
            String slotFile = stack.getTag().getString("schematic_file");
            if (!slotFile.equals(selectedFile)) {
                selectedFile = slotFile;
                cachedTemplate = null;
                VaporUtilsPacketHandler.INSTANCE.sendToServer(new RequestMaterialsPacket(menu.getBlockEntity().getBlockPos(), selectedFile));
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    // 1. 왼쪽 파일 리스트
    private void renderFileList(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = leftPos + 15;
        int y = topPos + 55;

        // 아저씨가 날려먹은 제목 그리기! (번역 키 적용 + 진한 회색)
        graphics.drawString(this.font, Component.translatable("gui.vapor_utils.files").getString(), x, y - 12, 0x404040, false);

        graphics.enableScissor(x, y, x + 70, y + 70);
        for (int i = 0; i < fileList.size(); i++) {
            int itemY = y + (i * 12) - fileScrollOffset;
            String name = fileList.get(i);

            // 선택된 건 파란색, 기본은 진한 회색!
            int color = name.equals(selectedFile) ? 0x0000FF : 0x404040;
            String displayName = name.length() > 11 ? name.substring(0, 9) + ".." : name;

            graphics.drawString(this.font, displayName, x + 2, itemY, color, false);
        }
        graphics.disableScissor();
    }

    private void renderPreview(GuiGraphics graphics, float partialTick) {
        int pX = leftPos + 95;
        int pY = topPos + 50;

        if (selectedFile.isEmpty()) return;
        if (cachedTemplate == null) cachedTemplate = loadTemplate(selectedFile);
        if (cachedTemplate == null) return;

        graphics.enableScissor(pX, pY, pX + 86, pY + 86);
        PoseStack pose = graphics.pose();
        pose.pushPose();

        pose.translate(pX + 43, pY + 50, 200);

        float maxDim = Math.max(cachedTemplate.getSize().getX(), Math.max(cachedTemplate.getSize().getY(), cachedTemplate.getSize().getZ()));
        float finalScale = 35f / Math.max(1, maxDim);
        pose.scale(finalScale, -finalScale, finalScale);

        previewRotation += partialTick * 0.8f;
        pose.mulPose(Axis.XP.rotationDegrees(22));
        pose.mulPose(Axis.YP.rotationDegrees(previewRotation));
        pose.translate(-cachedTemplate.getSize().getX()/2f, -cachedTemplate.getSize().getY()/2f, -cachedTemplate.getSize().getZ()/2f);

        Lighting.setupFor3DItems();
        var buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        for (var info : cachedTemplate.palettes.get(0).blocks()) {
            if (info.state().getRenderShape() == RenderShape.MODEL) {
                pose.pushPose();
                pose.translate(info.pos().getX(), info.pos().getY(), info.pos().getZ());
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(info.state(), pose, buffer, 15728880, OverlayTexture.NO_OVERLAY);
                pose.popPose();
            }
        }
        buffer.endBatch();
        pose.popPose();
        graphics.disableScissor();
    }

    // 2. 오른쪽 재료 리스트
    private void renderMaterials(GuiGraphics graphics) {
        int x = leftPos + 195;
        int y = topPos + 55;

        // 아저씨가 또 날려먹은 제목 그리기! (번역 키 적용 + 진한 회색)
        graphics.drawString(this.font, Component.translatable("gui.vapor_utils.needed").getString(), x, y - 12, 0x404040, false);

        int i = 0;
        for (var entry : materialList.entrySet()) {
            if (i > 8) break;

            var block = BuiltInRegistries.BLOCK.get(new ResourceLocation(entry.getKey()));
            String name = block.getName().getString();
            String prefix = entry.getValue() + "x ";

            String text = prefix + name;

            // 밀랍칠한 어쩌고 하는 변태 같은 긴 이름 자르기!
            while (this.font.width(text) > 72 && name.length() > 1) {
                name = name.substring(0, name.length() - 1);
                text = prefix + name + ".";
            }

            graphics.drawString(this.font, text, x + 1, y + (i * 12), 0x404040, false);
            i++;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 280, 240);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        fileScrollOffset = Math.max(0, fileScrollOffset - (int)delta * 12);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listX = leftPos + 15;
        int listY = topPos + 55;
        if (mouseX >= listX && mouseX <= listX + 70 && mouseY >= listY && mouseY <= listY + 70) {
            int index = (int)((mouseY - listY + fileScrollOffset) / 12);
            if (index >= 0 && index < fileList.size()) {
                selectedFile = fileList.get(index);
                cachedTemplate = null;
                VaporUtilsPacketHandler.INSTANCE.sendToServer(new RequestMaterialsPacket(menu.getBlockEntity().getBlockPos(), selectedFile));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private StructureTemplate loadTemplate(String fileName) {
        try {
            File file = new File(Minecraft.getInstance().gameDirectory, "schematics/vapor_utils/" + fileName);
            if (!file.exists()) return null;
            FileInputStream stream = new FileInputStream(file);
            CompoundTag nbt = NbtIo.readCompressed(stream);
            StructureTemplate t = new StructureTemplate();
            t.load(Minecraft.getInstance().level.holderLookup(BuiltInRegistries.BLOCK.key()), nbt);
            stream.close();
            return t;
        } catch (Exception e) { return null; }
    }
}