package com.bluedev.blockduplicatortree;

import com.bluedev.blockduplicatortree.block.DuplicatorLogBlock;
import com.bluedev.blockduplicatortree.block.entity.DuplicatorLogTileEntity;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = BlockDuplicatorTreeMod.MODID, name = BlockDuplicatorTreeMod.NAME, version = BlockDuplicatorTreeMod.VERSION)
public final class BlockDuplicatorTreeMod {
    public static final String MODID = "blockduplicatortree";
    public static final String NAME = "Block Duplicator Tree";
    public static final String VERSION = "0.1.0-alpha";

    public static DuplicatorLogBlock DUPLICATOR_LOG;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        DUPLICATOR_LOG = new DuplicatorLogBlock();
        GameRegistry.registerBlock(DUPLICATOR_LOG, "duplicator_log");
        GameRegistry.registerTileEntity(DuplicatorLogTileEntity.class, MODID + ":duplicator_log");
    }
}
