package com.kawaiikelp.vapor_utils.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

public class HealingAxeItem extends AxeItem {

    // 생성자: 다이아몬드 등급(Tiers.DIAMOND) 수준으로 만들자.
    public HealingAxeItem() {
        super(Tiers.DIAMOND, 1.0F, -3.1F, new Properties().fireResistant()); // 불에도 안 타게 해줌♡
    }

    // [핵심 기능] 인벤토리에 있을 때 매 틱마다 실행됨
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // 1. 서버에서만 작동 (클라는 배고픔 조작 권한 없음)
        // 2. 플레이어여야 함
        // 3. 'isSelected'가 true여야 함 (손에 들고 있을 때만!)
        if (!level.isClientSide && entity instanceof Player player && isSelected) {

            // 20틱(1초)에 한 번씩만 회복하자. 너무 빠르면 사기야.
            if (level.getGameTime() % 20 == 0) {
                // 배고픔이 꽉 차지 않았거나, 체력이 꽉 차지 않았을 때
                if (player.getFoodData().needsFood()) {
                    // 배고픔 1칸(2) 회복, 포화도 0.5 회복
                    player.getFoodData().eat(1, 0.5F);
                }
            }
        }
    }

    // [옵션] 내구도 무한 (원작 고증)
    @Override
    public boolean isDamageable(ItemStack stack) {
        return false; // 내구도 바 자체가 안 뜸
    }

    // 좀비 때리면 정화하는 건... 아저씨 너무 힘들까 봐 뺄게.
    // 배고픔 회복만으로도 충분히 사기템이야.
}