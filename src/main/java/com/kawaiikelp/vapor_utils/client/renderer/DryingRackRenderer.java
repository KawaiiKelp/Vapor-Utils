package com.kawaiikelp.vapor_utils.client.renderer;

import com.kawaiikelp.vapor_utils.block.DryingRackBlock;
import com.kawaiikelp.vapor_utils.block.entity.DryingRackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DryingRackRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {
    public DryingRackRenderer(BlockEntityRendererProvider.Context context) {
        // 생성자
        // 지금은 딱히 로드할 건 없음
    }

    @Override
    public void render(DryingRackBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        ItemStack itemStack = pBlockEntity.getItemHandler().getStackInSlot(0);

        if (itemStack.isEmpty()) return;

        pPoseStack.pushPose();

        Direction facing = pBlockEntity.getBlockState().getValue(DryingRackBlock.FACING);

        // 1. 위치 잡기
        // Y값을 조절해서 건조대 나무 부분에 예쁘게 걸치도록 해. (0.5는 정중앙)
        // 건조대 모델 높이에 따라서 0.6 ~ 0.7 정도로 조절해봐.
        pPoseStack.translate(0.5, 0.42, 0.5);

        // 2. 방향 돌리기 (건조대가 보는 방향에 맞춤)
        pPoseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        // [여기야!] 안으로(벽 쪽으로) 밀어넣기
        // 회전을 이미 했으니까, 이제 Z축이 '앞뒤'가 된 거야.
        // 0.0이면 정중앙.
        // -0.1, -0.2 처럼 '음수'를 넣으면 뒤(벽)로 가고,
        // +0.1 처럼 '양수'를 넣으면 앞으로 튀어나와.
        // 아저씨 취향대로 숫자를 조금씩 바꿔가면서 맞춰 봐. -0.15 정도 추천해 줄게♡
        pPoseStack.translate(0.0, 0.0, -0.4);

        // 3. 크기 조절
        pPoseStack.scale(0.75f, 0.75f, 0.75f);

        // 4. 렌더링
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);

        pPoseStack.popPose();
    }
}
