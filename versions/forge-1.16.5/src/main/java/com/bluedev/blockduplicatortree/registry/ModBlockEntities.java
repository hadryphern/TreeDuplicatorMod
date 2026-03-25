package com.bluedev.blockduplicatortree.registry;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.block.entity.DuplicatorLogBlockEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModBlockEntities {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES =
        DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, BlockDuplicatorTreeMod.MODID);

    public static final RegistryObject<TileEntityType<DuplicatorLogBlockEntity>> DUPLICATOR_LOG =
        TILE_ENTITIES.register("duplicator_log", () -> TileEntityType.Builder.of(DuplicatorLogBlockEntity::new, ModBlocks.DUPLICATOR_LOG.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        TILE_ENTITIES.register(modEventBus);
    }
}
