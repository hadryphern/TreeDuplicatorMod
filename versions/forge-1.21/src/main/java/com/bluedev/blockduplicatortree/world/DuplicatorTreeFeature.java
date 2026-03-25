package com.bluedev.blockduplicatortree.world;

import com.bluedev.blockduplicatortree.registry.ModBlocks;
import com.bluedev.blockduplicatortree.util.DuplicatorTreeStructure;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class DuplicatorTreeFeature extends Feature<NoneFeatureConfiguration> {
    public DuplicatorTreeFeature() {
        this(NoneFeatureConfiguration.CODEC);
    }

    public DuplicatorTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        for (int scanOffset = 8; scanOffset >= -8; scanOffset--) {
            BlockPos candidatePos = context.origin().above(scanOffset);
            if (!DuplicatorTreeStructure.canSpawnNaturally(context.level(), candidatePos, ModBlocks.DUPLICATOR_LOG.get(), ModBlocks.DUPLICATOR_LEAVES.get())) {
                continue;
            }

            BlockState logState = ModBlocks.DUPLICATOR_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
            BlockState leavesState = ModBlocks.DUPLICATOR_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.DISTANCE, Integer.valueOf(1))
                .setValue(LeavesBlock.PERSISTENT, Boolean.FALSE);

            context.level().setBlock(candidatePos, logState, 3);
            DuplicatorTreeStructure.placeTree(context.level(), candidatePos, logState, leavesState);
            return true;
        }

        return false;
    }
}
