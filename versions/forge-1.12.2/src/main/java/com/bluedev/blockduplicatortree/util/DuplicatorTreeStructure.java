package com.bluedev.blockduplicatortree.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

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

    public static boolean isValidSourceState(IBlockState state, Block logBlock, Block leavesBlock) {
        return !state.getMaterial().isReplaceable() || !state.getBlock().isAir(state, null, BlockPos.ORIGIN)
            ? state.getBlock() != logBlock && state.getBlock() != leavesBlock && !(state.getBlock() instanceof BlockLeaves)
            : false;
    }

    public static boolean isCompleteTree(IBlockAccess world, BlockPos basePos, Block logBlock, Block leavesBlock) {
        int y;
        for (y = 0; y < TRUNK_HEIGHT; y++) {
            if (world.getBlockState(basePos.up(y)).getBlock() != logBlock) {
                return false;
            }
        }

        for (BlockPos leafOffset : LEAF_OFFSETS) {
            if (!(world.getBlockState(basePos.add(leafOffset)).getBlock() instanceof BlockLeaves)) {
                return false;
            }
        }

        return true;
    }

    public static boolean canGrowPlacedTree(World world, BlockPos basePos, Block logBlock, Block leavesBlock) {
        return hasValidGround(world, basePos) && hasRoomForTree(world, basePos, logBlock, leavesBlock);
    }

    public static void placeTree(World world, BlockPos basePos, IBlockState logState, IBlockState leavesState) {
        int y;
        for (y = 1; y < TRUNK_HEIGHT; y++) {
            world.setBlockState(basePos.up(y), logState, 3);
        }

        for (BlockPos leafOffset : LEAF_OFFSETS) {
            world.setBlockState(basePos.add(leafOffset), leavesState, 3);
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

    public static boolean canPlaceDuplicate(World world, BlockPos targetPos, IBlockState sourceState, BlockPos sourcePos, Block logBlock, Block leavesBlock) {
        IBlockState currentState = world.getBlockState(targetPos);
        if (targetPos.equals(sourcePos)) {
            return false;
        }
        if (currentState.getBlock() == logBlock || currentState.getBlock() == leavesBlock || currentState.getBlock() instanceof BlockLeaves) {
            return false;
        }
        if (!currentState.getMaterial().isReplaceable() && !currentState.getBlock().isAir(currentState, world, targetPos)) {
            return false;
        }
        return sourceState.getBlock().canPlaceBlockAt(world, targetPos);
    }

    private static boolean hasValidGround(World world, BlockPos basePos) {
        BlockPos belowPos = basePos.down();
        IBlockState belowState = world.getBlockState(belowPos);
        return belowState.isSideSolid(world, belowPos, EnumFacing.UP);
    }

    private static boolean hasRoomForTree(World world, BlockPos basePos, Block logBlock, Block leavesBlock) {
        int y;
        if (!isReplaceable(world, basePos, logBlock, leavesBlock)) {
            return false;
        }

        for (y = 1; y < TRUNK_HEIGHT; y++) {
            if (!isReplaceable(world, basePos.up(y), logBlock, leavesBlock)) {
                return false;
            }
        }

        for (BlockPos leafOffset : LEAF_OFFSETS) {
            if (!isReplaceable(world, basePos.add(leafOffset), logBlock, leavesBlock)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isReplaceable(World world, BlockPos pos, Block logBlock, Block leavesBlock) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isAir(state, world, pos)
            || state.getMaterial().isReplaceable()
            || state.getBlock() == logBlock
            || state.getBlock() == leavesBlock
            || state.getBlock() instanceof BlockLeaves;
    }

    private static BlockPos offset(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }
}
