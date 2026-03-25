package com.bluedev.blockduplicatortree.registry;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.block.entity.DuplicatorLogBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static BlockEntityType<DuplicatorLogBlockEntity> DUPLICATOR_LOG;

    private ModBlockEntities() {
    }

    public static void register() {
        DUPLICATOR_LOG = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(BlockDuplicatorTreeMod.MODID, "duplicator_log"),
            FabricBlockEntityTypeBuilder.create(DuplicatorLogBlockEntity::new, ModBlocks.DUPLICATOR_LOG).build()
        );
    }
}
