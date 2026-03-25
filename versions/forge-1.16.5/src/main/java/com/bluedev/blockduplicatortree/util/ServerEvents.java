package com.bluedev.blockduplicatortree.util;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.block.entity.DuplicatorLogBlockEntity;
import com.bluedev.blockduplicatortree.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BlockDuplicatorTreeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerEvents {
    private ServerEvents() {
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getWorld() instanceof World)) {
            return;
        }

        World level = (World) event.getWorld();
        if (level.isClientSide) {
            return;
        }

        BlockState placedState = event.getPlacedBlock();
        if (!DuplicatorTreeStructure.isValidSourceState(placedState, ModBlocks.DUPLICATOR_LOG.get(), ModBlocks.DUPLICATOR_LEAVES.get())) {
            return;
        }

        BlockPos placedPos = event.getPos();
        for (Direction direction : Direction.values()) {
            BlockPos neighbourPos = placedPos.relative(direction);
            if (level.getBlockState(neighbourPos).getBlock() != ModBlocks.DUPLICATOR_LOG.get()) {
                continue;
            }

            BlockPos controllerPos = DuplicatorLogBlockEntity.findController(level, neighbourPos);
            TileEntity blockEntity = level.getBlockEntity(controllerPos);
            if (blockEntity instanceof DuplicatorLogBlockEntity && ((DuplicatorLogBlockEntity) blockEntity).tryStartDuplication(placedPos, placedState)) {
                return;
            }
        }
    }
}
