package com.bluedev.blockduplicatortree.registry;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.block.DuplicatorLeavesBlock;
import com.bluedev.blockduplicatortree.block.DuplicatorLogBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {
    public static final Block DUPLICATOR_LOG = new DuplicatorLogBlock(
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(2.0F)
            .sound(SoundType.WOOD)
            .ignitedByLava()
    );

    public static final Block DUPLICATOR_LEAVES = new DuplicatorLeavesBlock(
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .strength(0.2F)
            .randomTicks()
            .sound(SoundType.GRASS)
            .noOcclusion()
            .ignitedByLava()
            .pushReaction(PushReaction.DESTROY)
    );

    public static final Item DUPLICATOR_LOG_ITEM = new BlockItem(DUPLICATOR_LOG, new Item.Properties());
    public static final Item DUPLICATOR_LEAVES_ITEM = new BlockItem(DUPLICATOR_LEAVES, new Item.Properties());

    private ModBlocks() {
    }

    public static void register() {
        registerBlock("duplicator_log", DUPLICATOR_LOG);
        registerBlock("duplicator_leaves", DUPLICATOR_LEAVES);
        registerItem("duplicator_log", DUPLICATOR_LOG_ITEM);
        registerItem("duplicator_leaves", DUPLICATOR_LEAVES_ITEM);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
            entries.accept(DUPLICATOR_LOG_ITEM);
            entries.accept(DUPLICATOR_LEAVES_ITEM);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
            entries.accept(DUPLICATOR_LOG_ITEM);
            entries.accept(DUPLICATOR_LEAVES_ITEM);
        });
    }

    private static void registerBlock(String path, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, id(path), block);
    }

    private static void registerItem(String path, Item item) {
        Registry.register(BuiltInRegistries.ITEM, id(path), item);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(BlockDuplicatorTreeMod.MODID, path);
    }
}
