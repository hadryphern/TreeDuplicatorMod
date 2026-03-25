package com.bluedev.blockduplicatortree;

import com.bluedev.blockduplicatortree.registry.ModBlockEntities;
import com.bluedev.blockduplicatortree.registry.ModBlocks;
import com.bluedev.blockduplicatortree.util.ServerChunkGenerationHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;

public final class BlockDuplicatorTreeMod implements ModInitializer {
    public static final String MODID = "blockduplicatortree";

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModBlockEntities.register();

        ServerChunkEvents.CHUNK_LOAD.register(ServerChunkGenerationHandler::onChunkGenerate);
    }
}
