package com.bluedev.blockduplicatortree.util;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public final class DuplicatorTreeStructure {
    public static final int TRUNK_HEIGHT = 4;
    public static final int GROWTH_TICKS = 20 * 60 * 20;
    public static final int DUPLICATION_RADIUS = 2;
    public static final int DUPLICATION_TOTAL_TICKS = 20 * 60 * 10;
    public static final int DUPLICATION_STEP_TICKS = DUPLICATION_TOTAL_TICKS / 24;

    private static final int[][] LEAF_OFFSETS = new int[][] {
        {-1, 2, -2}, {0, 2, -2}, {1, 2, -2},
        {-2, 2, -1}, {-1, 2, -1}, {0, 2, -1}, {1, 2, -1}, {2, 2, -1},
        {-2, 2, 0}, {-1, 2, 0}, {1, 2, 0}, {2, 2, 0},
        {-2, 2, 1}, {-1, 2, 1}, {0, 2, 1}, {1, 2, 1}, {2, 2, 1},
        {-1, 2, 2}, {0, 2, 2}, {1, 2, 2},
        {-1, 3, -2}, {0, 3, -2}, {1, 3, -2},
        {-2, 3, -1}, {-1, 3, -1}, {0, 3, -1}, {1, 3, -1}, {2, 3, -1},
        {-2, 3, 0}, {-1, 3, 0}, {1, 3, 0}, {2, 3, 0},
        {-2, 3, 1}, {-1, 3, 1}, {0, 3, 1}, {1, 3, 1}, {2, 3, 1},
        {-1, 3, 2}, {0, 3, 2}, {1, 3, 2},
        {-1, 4, -1}, {0, 4, -1}, {1, 4, -1},
        {-1, 4, 0}, {0, 4, 0}, {1, 4, 0},
        {-1, 4, 1}, {0, 4, 1}, {1, 4, 1},
        {0, 5, 0}
    };

    private DuplicatorTreeStructure() {
    }

    public static boolean isValidSourceBlock(Block block, int metadata, Block logBlock) {
        return block != null
            && block != Blocks.air
            && block != logBlock
            && !(block instanceof BlockLeaves)
            && !block.hasTileEntity(metadata);
    }

    public static boolean isCompleteTree(World world, int baseX, int baseY, int baseZ, Block logBlock) {
        int y;
        for (y = 0; y < TRUNK_HEIGHT; y++) {
            if (world.getBlock(baseX, baseY + y, baseZ) != logBlock) {
                return false;
            }
        }

        for (y = 0; y < LEAF_OFFSETS.length; y++) {
            int[] offset = LEAF_OFFSETS[y];
            if (!(world.getBlock(baseX + offset[0], baseY + offset[1], baseZ + offset[2]) instanceof BlockLeaves)) {
                return false;
            }
        }

        return true;
    }

    public static boolean canGrowPlacedTree(World world, int baseX, int baseY, int baseZ, Block logBlock) {
        return hasValidGround(world, baseX, baseY, baseZ) && hasRoomForTree(world, baseX, baseY, baseZ, logBlock);
    }

    public static void placeTree(World world, int baseX, int baseY, int baseZ, Block logBlock) {
        int y;
        for (y = 1; y < TRUNK_HEIGHT; y++) {
            world.setBlock(baseX, baseY + y, baseZ, logBlock, 0, 3);
        }

        for (y = 0; y < LEAF_OFFSETS.length; y++) {
            int[] offset = LEAF_OFFSETS[y];
            world.setBlock(baseX + offset[0], baseY + offset[1], baseZ + offset[2], Blocks.leaves, 0, 3);
        }
    }

    public static List<int[]> buildDuplicationTargets(int baseX, int sourceY, int baseZ, int sourceX, int sourceZ) {
        List<int[]> targets = new ArrayList<int[]>();
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

                    int targetX = baseX + x;
                    int targetZ = baseZ + z;
                    if (targetX != sourceX || targetZ != sourceZ) {
                        targets.add(new int[] {targetX, sourceY, targetZ});
                    }
                }
            }
        }

        return targets;
    }

    public static int[][] buildSourceCandidates(int baseX, int baseY, int baseZ) {
        List<int[]> candidates = new ArrayList<int[]>();
        int y;

        for (y = 0; y < TRUNK_HEIGHT; y++) {
            int trunkY = baseY + y;
            candidates.add(new int[] {baseX - 1, trunkY, baseZ});
            candidates.add(new int[] {baseX + 1, trunkY, baseZ});
            candidates.add(new int[] {baseX, trunkY, baseZ - 1});
            candidates.add(new int[] {baseX, trunkY, baseZ + 1});
            candidates.add(new int[] {baseX, trunkY - 1, baseZ});
            candidates.add(new int[] {baseX, trunkY + 1, baseZ});
        }

        return candidates.toArray(new int[candidates.size()][]);
    }

    public static boolean canPlaceDuplicate(World world, int x, int y, int z, Block sourceBlock, int sourceMeta, int sourceX, int sourceY, int sourceZ, Block logBlock) {
        Block currentBlock = world.getBlock(x, y, z);
        if (x == sourceX && y == sourceY && z == sourceZ) {
            return false;
        }
        if (currentBlock == logBlock || currentBlock instanceof BlockLeaves) {
            return false;
        }
        if (!world.isAirBlock(x, y, z) && !currentBlock.isReplaceable(world, x, y, z)) {
            return false;
        }
        return sourceBlock.canPlaceBlockAt(world, x, y, z);
    }

    private static boolean hasValidGround(World world, int x, int y, int z) {
        return World.doesBlockHaveSolidTopSurface(world, x, y - 1, z);
    }

    private static boolean hasRoomForTree(World world, int baseX, int baseY, int baseZ, Block logBlock) {
        int y;
        if (!isReplaceable(world, baseX, baseY, baseZ, logBlock)) {
            return false;
        }

        for (y = 1; y < TRUNK_HEIGHT; y++) {
            if (!isReplaceable(world, baseX, baseY + y, baseZ, logBlock)) {
                return false;
            }
        }

        for (y = 0; y < LEAF_OFFSETS.length; y++) {
            int[] offset = LEAF_OFFSETS[y];
            if (!isReplaceable(world, baseX + offset[0], baseY + offset[1], baseZ + offset[2], logBlock)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isReplaceable(World world, int x, int y, int z, Block logBlock) {
        Block block = world.getBlock(x, y, z);
        return block == Blocks.air
            || block.isReplaceable(world, x, y, z)
            || block == logBlock
            || block instanceof BlockLeaves;
    }
}
