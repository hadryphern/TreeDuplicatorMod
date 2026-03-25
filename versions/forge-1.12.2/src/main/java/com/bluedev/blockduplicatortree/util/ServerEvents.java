package com.bluedev.blockduplicatortree.util;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.block.entity.DuplicatorLogTileEntity;
import com.bluedev.blockduplicatortree.registry.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = BlockDuplicatorTreeMod.MODID)
public final class ServerEvents {
    private ServerEvents() {
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.PlaceEvent event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }

        IBlockState placedState = event.getPlacedBlock();
        if (!DuplicatorTreeStructure.isValidSourceState(placedState, ModBlocks.DUPLICATOR_LOG, Blocks.LEAVES)) {
            return;
        }

        BlockPos placedPos = event.getPos();
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbourPos = placedPos.offset(facing);
            if (world.getBlockState(neighbourPos).getBlock() != ModBlocks.DUPLICATOR_LOG) {
                continue;
            }

            BlockPos controllerPos = DuplicatorLogTileEntity.findController(world, neighbourPos);
            TileEntity tileEntity = world.getTileEntity(controllerPos);
            if (tileEntity instanceof DuplicatorLogTileEntity && ((DuplicatorLogTileEntity) tileEntity).tryStartDuplication(placedPos, placedState)) {
                return;
            }
        }
    }
}
