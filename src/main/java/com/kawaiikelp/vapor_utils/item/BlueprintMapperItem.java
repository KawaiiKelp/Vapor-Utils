package com.kawaiikelp.vapor_utils.item;

import com.kawaiikelp.vapor_utils.block.entity.BlueprintWorkbenchBlockEntity;
import com.kawaiikelp.vapor_utils.registry.VaporUtilsItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class BlueprintMapperItem extends Item {
    public BlueprintMapperItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide || player == null) return InteractionResult.SUCCESS;

        BlockPos clickedPos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains("pos1")) {
            tag.put("pos1", NbtUtils.writeBlockPos(clickedPos));
            player.sendSystemMessage(Component.translatable("message.vapor_utils.mapper.pos1", clickedPos.toShortString()));
        } else {
            tag.put("pos2", NbtUtils.writeBlockPos(clickedPos));
            player.sendSystemMessage(Component.translatable("message.vapor_utils.mapper.pos2", clickedPos.toShortString()));
            player.sendSystemMessage(Component.translatable("message.vapor_utils.mapper.ready_to_work"));
        }
        return InteractionResult.SUCCESS;
    }

    // [패킷에서 호출하는 핵심 로직!]
    public static void saveSchematicToWorkbench(Level level, Player player, ItemStack mapperStack, BlueprintWorkbenchBlockEntity workbench, String customName) {
        if (level.isClientSide) return;

        CompoundTag tag = mapperStack.getOrCreateTag();
        if (!tag.contains("pos1") || !tag.contains("pos2")) {
            player.sendSystemMessage(Component.translatable("message.vapor_utils.mapper.no_pos"));
            return;
        }

        BlockPos pos1 = NbtUtils.readBlockPos(tag.getCompound("pos1"));
        BlockPos pos2 = NbtUtils.readBlockPos(tag.getCompound("pos2"));

        AABB area = new AABB(pos1, pos2);
        BlockPos min = new BlockPos((int) area.minX, (int) area.minY, (int) area.minZ);
        BlockPos size = new BlockPos((int) area.getXsize() + 1, (int) area.getYsize() + 1, (int) area.getZsize() + 1);

        // 너무 크면 아저씨 컴퓨터 폭발해♡ (48x48x48 제한)
        if (size.getX() * size.getY() * size.getZ() > 110592) {
            player.sendSystemMessage(Component.translatable("message.vapor_utils.blueprinter.too_big"));
            return;
        }

        StructureTemplate template = new StructureTemplate();
        template.fillFromWorld((ServerLevel) level, min, size, true, Blocks.AIR);

        try {
            Path schemDir = level.getServer().getServerDirectory().toPath().resolve("schematics").resolve("vapor_utils");
            if (!Files.exists(schemDir)) Files.createDirectories(schemDir);

            String safeName = customName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            if (safeName.isEmpty()) safeName = "unnamed_" + System.currentTimeMillis();
            String fileName = safeName + ".nbt";
            File file = schemDir.resolve(fileName).toFile();

            NbtIo.writeCompressed(template.save(new CompoundTag()), file);

            // [결과물 생성] 워크벤치 슬롯 1번에 쏙!
            ItemStack blueprintStack = new ItemStack(VaporUtilsItems.BLUEPRINT.get());
            CompoundTag blueprintTag = blueprintStack.getOrCreateTag();
            blueprintTag.putString("schematic_file", fileName);
            blueprintTag.putLong("schematic_size", (long)size.getX() * size.getY() * size.getZ());

            workbench.inventory.setStackInSlot(1, blueprintStack);

            // 기록기 좌표 초기화 (아저씨 일 끝났어, 이제 쉬어♡)
            tag.remove("pos1");
            tag.remove("pos2");

            player.sendSystemMessage(Component.translatable("message.vapor_utils.mapper.save_success", fileName));

        } catch (IOException e) {
            player.sendSystemMessage(Component.translatable("message.vapor_utils.mapper.error"));
            e.printStackTrace();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains("pos1")) tooltipComponents.add(Component.translatable("tooltip.vapor_utils.mapper.pos1_info", NbtUtils.readBlockPos(tag.getCompound("pos1")).toShortString()));
            if (tag.contains("pos2")) tooltipComponents.add(Component.translatable("tooltip.vapor_utils.mapper.pos2_info", NbtUtils.readBlockPos(tag.getCompound("pos2")).toShortString()));
        }
        tooltipComponents.add(Component.translatable("tooltip.vapor_utils.mapper.desc"));
    }
}