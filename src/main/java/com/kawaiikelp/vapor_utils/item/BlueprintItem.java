package com.kawaiikelp.vapor_utils.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlueprintItem extends Item {
    public BlueprintItem() {
        super(new Properties().stacksTo(1)); // 겹치면 데이터 꼬여. 1개만!
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("schematic_file")) {
            String fileName = tag.getString("schematic_file");
            tooltipComponents.add(Component.translatable("tooltip.vapor_utils.blueprint.file", fileName));

            if (tag.contains("schematic_size")) {
                long size = tag.getLong("schematic_size");
                tooltipComponents.add(Component.translatable("tooltip.vapor_utils.blueprint.size", size));
            }
        } else {
            tooltipComponents.add(Component.translatable("tooltip.vapor_utils.blueprint.empty"));
        }
    }
}