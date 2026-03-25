package com.bluedev.blockduplicatortree.registry;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.block.DuplicatorLeavesBlock;
import com.bluedev.blockduplicatortree.block.DuplicatorLogBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BlockDuplicatorTreeMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BlockDuplicatorTreeMod.MODID);

    public static final RegistryObject<Block> DUPLICATOR_LOG =
        BLOCKS.register("duplicator_log", () -> new DuplicatorLogBlock(AbstractBlock.Properties.copy(Blocks.OAK_LOG)));

    public static final RegistryObject<Block> DUPLICATOR_LEAVES =
        BLOCKS.register("duplicator_leaves", () -> new DuplicatorLeavesBlock(AbstractBlock.Properties.copy(Blocks.OAK_LEAVES).randomTicks().noOcclusion()));

    public static final RegistryObject<Item> DUPLICATOR_LOG_ITEM =
        ITEMS.register("duplicator_log", () -> new BlockItem(DUPLICATOR_LOG.get(), new Item.Properties().tab(ItemGroup.TAB_BUILDING_BLOCKS)));

    public static final RegistryObject<Item> DUPLICATOR_LEAVES_ITEM =
        ITEMS.register("duplicator_leaves", () -> new BlockItem(DUPLICATOR_LEAVES.get(), new Item.Properties().tab(ItemGroup.TAB_BUILDING_BLOCKS)));

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
