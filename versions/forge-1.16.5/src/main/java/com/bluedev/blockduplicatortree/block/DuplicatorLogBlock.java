package com.bluedev.blockduplicatortree.block;

import com.bluedev.blockduplicatortree.block.entity.DuplicatorLogBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public final class DuplicatorLogBlock extends RotatedPillarBlock {
    public DuplicatorLogBlock(AbstractBlock.Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader level) {
        return new DuplicatorLogBlockEntity();
    }

    @Override
    public void setPlacedBy(World level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide || placer == null) {
            return;
        }

        TileEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DuplicatorLogBlockEntity) {
            ((DuplicatorLogBlockEntity) blockEntity).markPlacedByPlayer();
        }
    }
}
