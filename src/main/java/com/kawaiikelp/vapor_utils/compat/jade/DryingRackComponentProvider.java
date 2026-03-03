package com.kawaiikelp.vapor_utils.compat.jade;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.block.entity.DryingRackBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.IProgressStyle;

public enum DryingRackComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    // 고유 ID (이름 겹치면 안 됨)
    public static final ResourceLocation UID = new ResourceLocation(VaporUtils.MODID, "drying_rack_info");

    // [서버 쪽] : 데이터를 포장해서 보내라!
    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (blockEntity instanceof DryingRackBlockEntity rack) {
            // 아까 만든 Getter 메서드 쓰는 거야. 또 까먹지 말고!
            data.putInt("progress", rack.getProgress());
            data.putInt("totalTicks", rack.getTotalTicks());
        }
    }

    // [클라이언트 쪽] : 받은 데이터를 화면에 그려라!
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        // 서버에서 온 데이터 꾸러미(NBT)를 깐다
        CompoundTag data = accessor.getServerData();

        // 데이터가 잘 들어있나 확인
        if (data.contains("progress") && data.contains("totalTicks")) {
            int progress = data.getInt("progress");
            int total = data.getInt("totalTicks");

            // 전체 시간이 0보다 클 때만 (즉, 건조 중일때만) 표시
            if (total > 0) {
                // 1. 진행률 계산 (0.0 ~ 1.0 사이의 소수점 값이어야 해)
                float ratio = (float) progress / total;
                if (ratio > 1.0f) ratio = 1.0f; // 100 넘으면 자르기

                // 2. 도우미(Helper) 소환
                IElementHelper helper = tooltip.getElementHelper();

                // 3. 막대기 스타일 설정
                // progressStyle()이 기본 스타일을 줘.
                // .color(완료된색, 미완료색) -> 색깔 바꾸고 싶으면 바꿔.
                // 여기선 불타는 듯한 주황색으로 해줄게♡ (아저씨 마음처럼?)
                IProgressStyle style = helper.progressStyle().color(0xFFFFAA00, 0xFF553300);

                // 4. 표시할 텍스트 (아까 등록한 번역 키 사용!)
                // "건조 중 50%" 이런 식으로 나올 거야.
                Component text = Component.translatable("jade.vapor_utils.drying");
                // 퍼센트 숫자도 같이 보여주고 싶으면 뒤에 append 하거나, Jade가 알아서 보여주기도 해.
                // 일단 심플하게 텍스트만 넣자. (Jade가 진행도 텍스트를 자동 병합해주기도 함!)

                // 5. 툴팁에 막대기 추가!
                // helper.progress(비율, 텍스트, 스타일, 박스스타일, 세로모드여부)
                tooltip.add(helper.progress(ratio, text, style, BoxStyle.DEFAULT, false));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
