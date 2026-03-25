package com.bluedev.blockduplicatortree.registry;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.block.DuplicatorLogBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BlockDuplicatorTreeMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BlockDuplicatorTreeMod.MODID);

    public static final RegistryObject<Block> DUPLICATOR_LOG =
        BLOCKS.register(
            "duplicator_log",
            () -> new DuplicatorLogBlock(
                BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()
            )
        );

    public static final RegistryObject<Block> DUPLICATOR_LEAVES =
        BLOCKS.register(
            "duplicator_leaves",
            () -> new TintedParticleLeavesBlock(
                0.01F,
                BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .strength(0.2F)
                    .randomTicks()
                    .sound(SoundType.GRASS)
                    .noOcclusion()
                    .ignitedByLava()
                    .pushReaction(PushReaction.DESTROY)
            )
        );

    public static final RegistryObject<Item> DUPLICATOR_LOG_ITEM =
        ITEMS.register("duplicator_log", () -> new BlockItem(DUPLICATOR_LOG.get(), new Item.Properties()));

    public static final RegistryObject<Item> DUPLICATOR_LEAVES_ITEM =
        ITEMS.register("duplicator_leaves", () -> new BlockItem(DUPLICATOR_LEAVES.get(), new Item.Properties()));

    private ModBlocks() {
    }

    public static void register(BusGroup modBusGroup) {
        BLOCKS.register(modBusGroup);
        ITEMS.register(modBusGroup);
    }
}
