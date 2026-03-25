package com.bluedev.blockduplicatortree.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

public final class DuplicatorTreeStructure {
    public static final int TRUNK_HEIGHT = 4;
    public static final int GROWTH_TICKS = 20 * 60 * 20;
    public static final int DUPLICATION_RADIUS = 2;
    public static final int DUPLICATION_TOTAL_TICKS = 20 * 60 * 10;
    public static final int DUPLICATION_STEP_TICKS = DUPLICATION_TOTAL_TICKS / 24;

    private static final List<BlockPos> LEAF_OFFSETS = Collections.unmodifiableList(Arrays.asList(
        offset(-1, 2, -2), offset(0, 2, -2), offset(1, 2, -2),
        offset(-2, 2, -1), offset(-1, 2, -1), offset(0, 2, -1), offset(1, 2, -1), offset(2, 2, -1),
        offset(-2, 2, 0), offset(-1, 2, 0), offset(1, 2, 0), offset(2, 2, 0),
        offset(-2, 2, 1), offset(-1, 2, 1), offset(0, 2, 1), offset(1, 2, 1), offset(2, 2, 1),
        offset(-1, 2, 2), offset(0, 2, 2), offset(1, 2, 2),
        offset(-1, 3, -2), offset(0, 3, -2), offset(1, 3, -2),
        offset(-2, 3, -1), offset(-1, 3, -1), offset(0, 3, -1), offset(1, 3, -1), offset(2, 3, -1),
        offset(-2, 3, 0), offset(-1, 3, 0), offset(1, 3, 0), offset(2, 3, 0),
        offset(-2, 3, 1), offset(-1, 3, 1), offset(0, 3, 1), offset(1, 3, 1), offset(2, 3, 1),
        offset(-1, 3, 2), offset(0, 3, 2), offset(1, 3, 2),
        offset(-1, 4, -1), offset(0, 4, -1), offset(1, 4, -1),
        offset(-1, 4, 0), offset(0, 4, 0), offset(1, 4, 0),
        offset(-1, 4, 1), offset(0, 4, 1), offset(1, 4, 1),
        offset(0, 5, 0)
    ));

    private DuplicatorTreeStructure() {
    }

    public static boolean isValidSourceState(BlockState state, Block logBlock, Block leavesBlock) {
        return !state.isAir()
            && state.getBlock() != logBlock
            && state.getBlock() != leavesBlock
            && !state.hasTileEntity();
    }

    public static boolean isCompleteTree(IWorldReader level, BlockPos basePos, Block logBlock, Block leavesBlock) {
        int y;
        for (y = 0; y < TRUNK_HEIGHT; y++) {
            if (level.getBlockState(basePos.above(y)).getBlock() != logBlock) {
                return false;
            }
        }

        for (BlockPos leafOffset : LEAF_OFFSETS) {
            if (level.getBlockState(basePos.offset(leafOffset)).getBlock() != leavesBlock) {
                return false;
            }
        }

        return true;
    }

    public static boolean canGrowPlacedTree(IWorldReader level, BlockPos basePos, Block logBlock, Block leavesBlock) {
        return hasValidGround(level, basePos) && hasRoomForTree(level, basePos, logBlock, leavesBlock);
    }

    public static void placeTree(IWorld level, BlockPos basePos, BlockState logState, BlockState leavesState) {
        int y;
        for (y = 1; y < TRUNK_HEIGHT; y++) {
            level.setBlock(basePos.above(y), logState, 3);
        }

        for (BlockPos leafOffset : LEAF_OFFSETS) {
            level.setBlock(basePos.offset(leafOffset), leavesState, 3);
        }
    }

    public static List<BlockPos> buildDuplicationTargets(BlockPos basePos, BlockPos sourcePos) {
        List<BlockPos> targets = new ArrayList<BlockPos>();
        int y = sourcePos.getY();
        int radius;
        int x;
        int z;

        for (radius = 0; radius <= DUPLICATION_RADIUS; radius++) {
            for (x = -radius; x <= radius; x++) {
                for (z = -radius; z <= radius; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) != radius) {
                        continue;
                    }
                    if (x == 0 && z == 0) {
                        continue;
                    }

                    BlockPos candidate = new BlockPos(basePos.getX() + x, y, basePos.getZ() + z);
                    if (!candidate.equals(sourcePos)) {
                        targets.add(candidate);
                    }
                }
            }
        }

        return targets;
    }

    public static boolean canPlaceDuplicate(IWorldReader level, BlockPos targetPos, BlockState sourceState, BlockPos sourcePos, Block logBlock, Block leavesBlock) {
        BlockState currentState = level.getBlockState(targetPos);
        if (targetPos.equals(sourcePos)) {
            return false;
        }
        if (currentState.getBlock() == logBlock || currentState.getBlock() == leavesBlock) {
            return false;
        }
        if (!currentState.isAir() && !currentState.getMaterial().isReplaceable()) {
            return false;
        }
        return sourceState.canSurvive(level, targetPos);
    }

    private static boolean hasValidGround(IWorldReader level, BlockPos basePos) {
        BlockPos belowPos = basePos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return belowState.isFaceSturdy(level, belowPos, Direction.UP);
    }

    private static boolean hasRoomForTree(IWorldReader level, BlockPos basePos, Block logBlock, Block leavesBlock) {
        int y;
        if (!isReplaceable(level, basePos, logBlock, leavesBlock)) {
            return false;
        }

        for (y = 1; y < TRUNK_HEIGHT; y++) {
            if (!isReplaceable(level, basePos.above(y), logBlock, leavesBlock)) {
                return false;
            }
        }

        for (BlockPos leafOffset : LEAF_OFFSETS) {
            if (!isReplaceable(level, basePos.offset(leafOffset), logBlock, leavesBlock)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isReplaceable(IWorldReader level, BlockPos pos, Block logBlock, Block leavesBlock) {
        BlockState state = level.getBlockState(pos);
        return state.isAir()
            || state.getMaterial().isReplaceable()
            || state.getBlock() == logBlock
            || state.getBlock() == leavesBlock;
    }

    private static BlockPos offset(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }
}
