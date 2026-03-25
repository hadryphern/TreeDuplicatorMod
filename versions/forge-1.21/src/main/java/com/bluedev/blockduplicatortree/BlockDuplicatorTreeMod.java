package com.bluedev.blockduplicatortree;

import com.bluedev.blockduplicatortree.registry.ModBlockEntities;
import com.bluedev.blockduplicatortree.registry.ModBlocks;
import com.bluedev.blockduplicatortree.registry.ModFeatures;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BlockDuplicatorTreeMod.MODID)
public final class BlockDuplicatorTreeMod {
    public static final String MODID = "blockduplicatortree";

    public BlockDuplicatorTreeMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModFeatures.register(modEventBus);

        modEventBus.addListener(this::addCreativeTabEntries);
    }

    private void addCreativeTabEntries(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS || event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModBlocks.DUPLICATOR_LOG_ITEM.get());
            event.accept(ModBlocks.DUPLICATOR_LEAVES_ITEM.get());
        }
    }
}
