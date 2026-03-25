package com.bluedev.blockduplicatortree.registry;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.block.entity.DuplicatorLogBlockEntity;
import java.util.Set;
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
            () -> new BlockEntityType<>(DuplicatorLogBlockEntity::new, Set.of(ModBlocks.DUPLICATOR_LOG.get()))
        );

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
