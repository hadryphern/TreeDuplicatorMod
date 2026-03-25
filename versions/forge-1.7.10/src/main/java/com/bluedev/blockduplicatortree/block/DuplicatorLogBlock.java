package com.bluedev.blockduplicatortree.block;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.block.entity.DuplicatorLogTileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public final class DuplicatorLogBlock extends BlockContainer {
    public DuplicatorLogBlock() {
        super(Material.wood);
        this.setBlockName(BlockDuplicatorTreeMod.MODID + ".duplicator_log");
        this.setHardness(2.0F);
        this.setResistance(2.0F);
        this.setStepSound(Block.soundTypeWood);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new DuplicatorLogTileEntity();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, placer, stack);

        if (world.isRemote || placer == null) {
            return;
        }

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof DuplicatorLogTileEntity) {
            ((DuplicatorLogTileEntity) tileEntity).markPlacedByPlayer();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        return Blocks.log.getIcon(side, 0);
    }
}
