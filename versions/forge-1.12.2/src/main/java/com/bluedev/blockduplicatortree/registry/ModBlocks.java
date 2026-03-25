package com.bluedev.blockduplicatortree.registry;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.block.DuplicatorLogBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = BlockDuplicatorTreeMod.MODID)
public final class ModBlocks {
    public static final DuplicatorLogBlock DUPLICATOR_LOG = new DuplicatorLogBlock();
    public static final ItemBlock DUPLICATOR_LOG_ITEM = new ItemBlock(DUPLICATOR_LOG);

    static {
        DUPLICATOR_LOG_ITEM.setRegistryName(DUPLICATOR_LOG.getRegistryName());
    }

    private ModBlocks() {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(DUPLICATOR_LOG);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(DUPLICATOR_LOG_ITEM);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(DUPLICATOR_LOG_ITEM, 0, new ModelResourceLocation(DUPLICATOR_LOG.getRegistryName(), "inventory"));
    }
}
