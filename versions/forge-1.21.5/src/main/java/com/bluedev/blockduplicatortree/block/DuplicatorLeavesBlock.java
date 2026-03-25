package com.bluedev.blockduplicatortree.block;

import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class DuplicatorLeavesBlock extends TintedParticleLeavesBlock {
    public DuplicatorLeavesBlock(BlockBehaviour.Properties properties) {
        super(0.01F, properties);
    }
}
