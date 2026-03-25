package com.bluedev.blockduplicatortree.util;

import com.bluedev.blockduplicatortree.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public final class ServerChunkGenerationHandler {
    private ServerChunkGenerationHandler() {
    }

    public static void onChunkGenerate(ServerLevel level, LevelChunk chunk) {
        BlockPos spawnPos = DuplicatorTreeStructure.findNaturalSpawnInChunk(
            level,
            chunk.getPos(),
            level.getSeed(),
            ModBlocks.DUPLICATOR_LOG,
            ModBlocks.DUPLICATOR_LEAVES
        );
        if (spawnPos == null) {
            return;
        }

        BlockState logState = ModBlocks.DUPLICATOR_LOG.defaultBlockState()
            .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState leavesState = ModBlocks.DUPLICATOR_LEAVES.defaultBlockState()
            .setValue(LeavesBlock.DISTANCE, Integer.valueOf(1))
            .setValue(LeavesBlock.PERSISTENT, Boolean.FALSE);

        level.setBlock(spawnPos, logState, 3);
        DuplicatorTreeStructure.placeTree(level, spawnPos, logState, leavesState);
    }
}
