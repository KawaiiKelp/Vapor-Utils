package com.kawaiikelp.vapor_utils.registry;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.item.BlueprintItem;
import com.kawaiikelp.vapor_utils.item.BlueprintMapperItem;
import com.kawaiikelp.vapor_utils.item.HealingAxeItem;
import com.kawaiikelp.vapor_utils.item.ProjectorItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VaporUtilsItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, VaporUtils.MODID);

    static {
        // 블록 Map을 뒤져서 아이템으로 등록!
        VaporUtilsBlocks.DRYING_RACKS.forEach((wood, blockReg) -> {
            ITEMS.register(wood + "_drying_rack",
                    () -> new BlockItem(blockReg.get(), new Item.Properties()));
        });
    }

    // [추가] 회복의 도끼 등록
    public static final RegistryObject<Item> HEALING_AXE = ITEMS.register("healing_axe",
            () -> new HealingAxeItem());

    // [추가] 청사진 아이템
    public static final RegistryObject<Item> BLUEPRINT_MAPPER = ITEMS.register("blueprint_mapper", BlueprintMapperItem::new);
    public static final RegistryObject<Item> BLUEPRINT_WORKBENCH_ITEM = ITEMS.register("blueprint_workbench",
            () -> new BlockItem(VaporUtilsBlocks.BLUEPRINT_WORKBENCH.get(), new Item.Properties()) {
                // 툴팁 오버라이드!
                @Override
                public void appendHoverText(ItemStack stack, @Nullable net.minecraft.world.level.Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.vapor_utils.workbench.desc"));
                }
            });

    // [추가] 투영기 아이템
    public static final RegistryObject<Item> PROJECTOR = ITEMS.register("projector",
            () -> new ProjectorItem());

    // [추가] 청사진 (Blueprint)
    public static final RegistryObject<Item> BLUEPRINT = ITEMS.register("blueprint",
            () -> new BlueprintItem());
}
