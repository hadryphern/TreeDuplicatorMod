package com.bluedev.blockduplicatortree.util;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.block.entity.DuplicatorLogBlockEntity;
import com.bluedev.blockduplicatortree.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BlockDuplicatorTreeMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerEvents {
    private ServerEvents() {
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide) {
            return;
        }

        BlockState placedState = event.getPlacedBlock();
        if (!DuplicatorTreeStructure.isValidSourceState(placedState, ModBlocks.DUPLICATOR_LOG.get(), ModBlocks.DUPLICATOR_LEAVES.get())) {
            return;
        }

        BlockPos placedPos = event.getPos();
        for (Direction direction : Direction.values()) {
            BlockPos neighbourPos = placedPos.relative(direction);
            if (!level.getBlockState(neighbourPos).is(ModBlocks.DUPLICATOR_LOG.get())) {
                continue;
            }

            BlockPos controllerPos = DuplicatorLogBlockEntity.findController(level, neighbourPos);
            BlockEntity blockEntity = level.getBlockEntity(controllerPos);
            if (blockEntity instanceof DuplicatorLogBlockEntity duplicatorLog && duplicatorLog.tryStartDuplication(placedPos, placedState)) {
                return;
            }
        }
    }
}
