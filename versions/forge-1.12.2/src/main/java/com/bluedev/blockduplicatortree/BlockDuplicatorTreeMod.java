package com.bluedev.blockduplicatortree;

import com.bluedev.blockduplicatortree.block.entity.DuplicatorLogTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = BlockDuplicatorTreeMod.MODID, name = BlockDuplicatorTreeMod.NAME, version = BlockDuplicatorTreeMod.VERSION)
public final class BlockDuplicatorTreeMod {
    public static final String MODID = "blockduplicatortree";
    public static final String NAME = "Block Duplicator Tree";
    public static final String VERSION = "0.1.0-alpha";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerTileEntity(DuplicatorLogTileEntity.class, new ResourceLocation(MODID, "duplicator_log"));
    }
}
