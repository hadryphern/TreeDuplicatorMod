package com.bluedev.blockduplicatortree.block;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.block.entity.DuplicatorLogTileEntity;
import javax.annotation.Nullable;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public final class DuplicatorLogBlock extends BlockRotatedPillar {
    public DuplicatorLogBlock() {
        super(Material.WOOD);
        this.setRegistryName(BlockDuplicatorTreeMod.MODID, "duplicator_log");
        this.setUnlocalizedName(BlockDuplicatorTreeMod.MODID + ".duplicator_log");
        this.setHardness(2.0F);
        this.setResistance(2.0F);
        this.setSoundType(SoundType.WOOD);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.Y));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new DuplicatorLogTileEntity();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, @Nullable EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        if (world.isRemote || placer == null) {
            return;
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof DuplicatorLogTileEntity) {
            ((DuplicatorLogTileEntity) tileEntity).markPlacedByPlayer();
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = this.getDefaultState();
        int axisBits = meta & 12;
        if (axisBits == 4) {
            return state.withProperty(AXIS, EnumFacing.Axis.X);
        }
        if (axisBits == 8) {
            return state.withProperty(AXIS, EnumFacing.Axis.Z);
        }
        return state.withProperty(AXIS, EnumFacing.Axis.Y);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing.Axis axis = state.getValue(AXIS);
        if (axis == EnumFacing.Axis.X) {
            return 4;
        }
        if (axis == EnumFacing.Axis.Z) {
            return 8;
        }
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AXIS);
    }
}
