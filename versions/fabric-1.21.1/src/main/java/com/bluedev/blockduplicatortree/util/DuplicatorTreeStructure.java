package com.bluedev.blockduplicatortree.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class DuplicatorTreeStructure {
    public static final int TRUNK_HEIGHT = 4;
    public static final int GROWTH_TICKS = 20 * 60 * 20;
    public static final int DUPLICATION_RADIUS = 2;
    public static final int DUPLICATION_TOTAL_TICKS = 20 * 60 * 10;
    public static final int DUPLICATION_STEP_TICKS = DUPLICATION_TOTAL_TICKS / 24;
    public static final int NATURAL_RARITY_CHUNKS = 384;

    private static final List<BlockPos> LEAF_OFFSETS = List.of(
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
    );

    private DuplicatorTreeStructure() {
    }

    public static boolean isValidSourceState(BlockState state, Block logBlock, Block leavesBlock) {
        return !state.isAir()
            && !state.is(logBlock)
            && !state.is(leavesBlock)
            && !state.hasBlockEntity();
    }

    public static boolean isCompleteTree(LevelReader level, BlockPos basePos, Block logBlock, Block leavesBlock) {
        for (int y = 0; y < TRUNK_HEIGHT; y++) {
            if (!level.getBlockState(basePos.above(y)).is(logBlock)) {
                return false;
            }
        }

        for (BlockPos leafOffset : LEAF_OFFSETS) {
            if (!level.getBlockState(basePos.offset(leafOffset)).is(leavesBlock)) {
                return false;
            }
        }

        return true;
    }

    public static boolean canGrowPlacedTree(LevelReader level, BlockPos basePos, Block logBlock, Block leavesBlock) {
        return hasValidGround(level, basePos) && hasRoomForTree(level, basePos, logBlock, leavesBlock);
    }

    public static boolean canSpawnNaturally(LevelReader level, BlockPos basePos, Block logBlock, Block leavesBlock) {
        return level.getBiome(basePos).is(Biomes.LUSH_CAVES)
            && !level.canSeeSky(basePos)
            && hasValidGround(level, basePos)
            && hasRoomForTree(level, basePos, logBlock, leavesBlock)
            && hasCeilingAbove(level, basePos.above(TRUNK_HEIGHT + 1), 12);
    }

    public static void placeTree(LevelAccessor level, BlockPos basePos, BlockState logState, BlockState leavesState) {
        for (int y = 1; y < TRUNK_HEIGHT; y++) {
            level.setBlock(basePos.above(y), logState, 3);
        }

        for (BlockPos leafOffset : LEAF_OFFSETS) {
            level.setBlock(basePos.offset(leafOffset), leavesState, 3);
        }
    }

    public static List<BlockPos> buildDuplicationTargets(BlockPos basePos, BlockPos sourcePos) {
        List<BlockPos> targets = new ArrayList<>();
        int y = sourcePos.getY();

        for (int radius = 0; radius <= DUPLICATION_RADIUS; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
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

    public static List<BlockPos> buildSourceCandidates(BlockPos basePos) {
        Set<BlockPos> candidates = new LinkedHashSet<>();

        for (int y = 0; y < TRUNK_HEIGHT; y++) {
            BlockPos trunkPos = basePos.above(y);
            for (Direction direction : Direction.values()) {
                candidates.add(trunkPos.relative(direction));
            }
        }

        return List.copyOf(candidates);
    }

    public static BlockPos findNaturalSpawnInChunk(LevelReader level, ChunkPos chunkPos, long worldSeed, Block logBlock, Block leavesBlock) {
        long randomSeed = worldSeed ^ (chunkPos.x * 341873128712L) ^ (chunkPos.z * 132897987541L);
        RandomSource random = RandomSource.create(randomSeed);
        if (random.nextInt(NATURAL_RARITY_CHUNKS) != 0) {
            return null;
        }

        int minY = level.getMinBuildHeight() + 4;
        int maxY = level.getMinBuildHeight() + level.getHeight() - TRUNK_HEIGHT - 3;
        for (int attempt = 0; attempt < 6; attempt++) {
            int x = chunkPos.getMinBlockX() + random.nextInt(16);
            int z = chunkPos.getMinBlockZ() + random.nextInt(16);

            for (int y = minY; y <= maxY; y++) {
                BlockPos candidatePos = new BlockPos(x, y, z);
                if (canSpawnNaturally(level, candidatePos, logBlock, leavesBlock)) {
                    return candidatePos;
                }
            }
        }

        return null;
    }

    public static boolean canPlaceDuplicate(
        LevelReader level,
        BlockPos targetPos,
        BlockState sourceState,
        BlockPos sourcePos,
        Block logBlock,
        Block leavesBlock
    ) {
        BlockState currentState = level.getBlockState(targetPos);
        if (targetPos.equals(sourcePos)) {
            return false;
        }
        if (currentState.is(logBlock) || currentState.is(leavesBlock)) {
            return false;
        }
        if (!currentState.isAir() && !currentState.canBeReplaced()) {
            return false;
        }
        return sourceState.canSurvive(level, targetPos);
    }

    private static boolean hasValidGround(LevelReader level, BlockPos basePos) {
        BlockPos belowPos = basePos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return belowState.isFaceSturdy(level, belowPos, Direction.UP);
    }

    private static boolean hasRoomForTree(LevelReader level, BlockPos basePos, Block logBlock, Block leavesBlock) {
        if (!isReplaceable(level, basePos, logBlock, leavesBlock)) {
            return false;
        }

        for (int y = 1; y < TRUNK_HEIGHT; y++) {
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

    private static boolean hasCeilingAbove(LevelReader level, BlockPos startPos, int maxDistance) {
        for (int i = 0; i <= maxDistance; i++) {
            if (!level.getBlockState(startPos.above(i)).isAir()) {
                return true;
            }
        }

        return false;
    }

    private static boolean isReplaceable(LevelReader level, BlockPos pos, Block logBlock, Block leavesBlock) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() || state.is(logBlock) || state.is(leavesBlock);
    }

    private static BlockPos offset(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }
}
