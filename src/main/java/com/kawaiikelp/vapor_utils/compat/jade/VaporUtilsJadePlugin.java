package com.kawaiikelp.vapor_utils.compat.jade;

import com.kawaiikelp.vapor_utils.block.DryingRackBlock;
import com.kawaiikelp.vapor_utils.block.entity.DryingRackBlockEntity;
import snownee.jade.api.*;

// Jade가 이 어노테이션 보고 찾아오는 거야.
@WailaPlugin
public class VaporUtilsJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        // 서버 데이터 제공자 (Server -> Client NBT 전송)
        registration.registerBlockDataProvider(DryingRackComponentProvider.INSTANCE, DryingRackBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // 클라이언트 툴팁 표시자 (Tooltip 렌더링)
        registration.registerBlockComponent(DryingRackComponentProvider.INSTANCE, DryingRackBlock.class);
    }
}