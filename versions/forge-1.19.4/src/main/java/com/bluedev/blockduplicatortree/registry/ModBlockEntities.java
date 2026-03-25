package com.bluedev.blockduplicatortree.registry;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.block.entity.DuplicatorLogBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BlockDuplicatorTreeMod.MODID);

    public static final RegistryObject<BlockEntityType<DuplicatorLogBlockEntity>> DUPLICATOR_LOG =
        BLOCK_ENTITIES.register(
            "duplicator_log",
            () -> BlockEntityType.Builder.of(DuplicatorLogBlockEntity::new, ModBlocks.DUPLICATOR_LOG.get()).build(null)
        );

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
