package com.bluedev.blockduplicatortree.block;

import com.bluedev.blockduplicatortree.block.entity.DuplicatorLogBlockEntity;
import com.bluedev.blockduplicatortree.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class DuplicatorLogBlock extends RotatedPillarBlock implements EntityBlock {
    public DuplicatorLogBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DuplicatorLogBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }

        if (type != ModBlockEntities.DUPLICATOR_LOG) {
            return null;
        }

        return (serverLevel, blockPos, blockState, blockEntity) ->
            DuplicatorLogBlockEntity.serverTick(serverLevel, blockPos, blockState, (DuplicatorLogBlockEntity) blockEntity);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide || placer == null) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DuplicatorLogBlockEntity duplicatorLog) {
            duplicatorLog.markPlacedByPlayer();
        }
    }
}
