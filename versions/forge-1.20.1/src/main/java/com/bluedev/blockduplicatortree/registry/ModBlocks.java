package com.bluedev.blockduplicatortree.registry;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.block.DuplicatorLeavesBlock;
import com.bluedev.blockduplicatortree.block.DuplicatorLogBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BlockDuplicatorTreeMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BlockDuplicatorTreeMod.MODID);

    public static final RegistryObject<Block> DUPLICATOR_LOG =
        BLOCKS.register("duplicator_log", () -> new DuplicatorLogBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG)));

    public static final RegistryObject<Block> DUPLICATOR_LEAVES =
        BLOCKS.register("duplicator_leaves", () -> new DuplicatorLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));

    public static final RegistryObject<Item> DUPLICATOR_LOG_ITEM =
        ITEMS.register("duplicator_log", () -> new BlockItem(DUPLICATOR_LOG.get(), new Item.Properties()));

    public static final RegistryObject<Item> DUPLICATOR_LEAVES_ITEM =
        ITEMS.register("duplicator_leaves", () -> new BlockItem(DUPLICATOR_LEAVES.get(), new Item.Properties()));

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
