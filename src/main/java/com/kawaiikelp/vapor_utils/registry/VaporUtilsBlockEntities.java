package com.kawaiikelp.vapor_utils.registry;

import com.kawaiikelp.vapor_utils.VaporUtils;
import com.kawaiikelp.vapor_utils.block.entity.BlueprintWorkbenchBlockEntity;
import com.kawaiikelp.vapor_utils.block.entity.DryingRackBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class VaporUtilsBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, VaporUtils.MODID);

    public static final RegistryObject<BlockEntityType<DryingRackBlockEntity>> DRYING_RACK_BE =
            BLOCK_ENTITIES.register("drying_rack", () ->
                    BlockEntityType.Builder.of(
                            DryingRackBlockEntity::new,
                            // [핵심] 여기에 모든 건조대 블록을 다 넣어줘야 해!
                            // Map에 있는 블록들을 배열로 싹 긁어모아서 던져주는 거야.
                            VaporUtilsBlocks.DRYING_RACKS.values().stream()
                                    .map(RegistryObject::get)
                                    .toArray(net.minecraft.world.level.block.Block[]::new)
                    ).build(null));

    public static final RegistryObject<BlockEntityType<BlueprintWorkbenchBlockEntity>> BLUEPRINT_WORKBENCH_BE =
            BLOCK_ENTITIES.register("blueprint_workbench", () ->
                    BlockEntityType.Builder.of(BlueprintWorkbenchBlockEntity::new, VaporUtilsBlocks.BLUEPRINT_WORKBENCH.get()).build(null));
}
