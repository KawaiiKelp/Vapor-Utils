package com.kawaiikelp.vapor_utils.item;

import com.kawaiikelp.vapor_utils.client.gui.menu.ProjectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProjectorItem extends Item {

    public ProjectorItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isCrouching()) {
            if (!context.getLevel().isClientSide) {
                BlockPos pos = context.getClickedPos().above();
                context.getItemInHand().getOrCreateTag().put("anchor", NbtUtils.writeBlockPos(pos));
                player.sendSystemMessage(Component.translatable("message.vapor_utils.projector.anchor", pos.toShortString()));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.isCrouching()) {
                // [서버] 토글 로직
                CompoundTag tag = stack.getOrCreateTag();
                boolean newState = !tag.getBoolean("active");

                if (newState) {
                    // 다른 투영기 끄기♡
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack s = player.getInventory().getItem(i);
                        if (s.getItem() instanceof ProjectorItem) s.getOrCreateTag().putBoolean("active", false);
                    }
                }
                tag.putBoolean("active", newState);
                player.sendSystemMessage(Component.translatable(newState ? "message.vapor_utils.projector.on" : "message.vapor_utils.projector.off"));
            } else {
                // GUI 열기♡
                NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider(
                        (id, inv, p) -> new ProjectorMenu(id, inv, stack),
                        Component.translatable("gui.vapor_utils.projector.title")
                ));
            }
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.getBoolean("active")) tooltipComponents.add(Component.translatable("tooltip.vapor_utils.projector.active"));
            if (tag.contains("anchor")) {
                BlockPos anchor = NbtUtils.readBlockPos(tag.getCompound("anchor"));
                tooltipComponents.add(Component.translatable("tooltip.vapor_utils.projector.anchor_pos", anchor.toShortString()));
            }
        }
        tooltipComponents.add(Component.translatable("tooltip.vapor_utils.projector.controls"));
        tooltipComponents.add(Component.translatable("tooltip.vapor_utils.projector.rotate"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("active");
    }
}