package com.kawaiikelp.vapor_utils.client.gui;

import com.kawaiikelp.vapor_utils.network.VaporUtilsPacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class SchematicNameScreen extends Screen {
    private final BlockPos pos2;
    private EditBox nameBox;

    public SchematicNameScreen(BlockPos pos2) {
        super(Component.translatable("gui.vapor_utils.schematic.save_title"));
        this.pos2 = pos2;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 텍스트 박스
        this.nameBox = new EditBox(this.font, centerX - 100, centerY - 20, 200, 20, Component.literal("Name"));
        this.nameBox.setMaxLength(50);
        this.addRenderableWidget(this.nameBox);
        this.setInitialFocus(this.nameBox); // 열리자마자 타자 칠 수 있게

        // 저장 버튼
        this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> save())
                .bounds(centerX - 50, centerY + 10, 100, 20)
                .build());
    }

    private void save() {
        String name = this.nameBox.getValue();
        if (name.isEmpty()) return; // 이름 없으면 무시

        // 패킷 발사! (이름이랑 좌표를 서버로 던짐)
        // VaporUtilsPacketHandler.INSTANCE.sendToServer(new SaveSchematicPacket(name, pos2));
        this.onClose(); // 창 닫기
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, "구조물 이름 입력", this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // 엔터키 누르면 저장되게
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            save();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ESC키로 닫기 등등은 super가 알아서 해줌
    @Override
    public boolean isPauseScreen() {
        return false; // 게임 멈추지 마
    }
}