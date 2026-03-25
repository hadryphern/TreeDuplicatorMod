package com.bluedev.blockduplicatortree.block.entity;

import com.bluedev.blockduplicatortree.registry.ModBlockEntities;
import com.bluedev.blockduplicatortree.registry.ModBlocks;
import com.bluedev.blockduplicatortree.util.DuplicatorTreeStructure;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class DuplicatorLogBlockEntity extends BlockEntity {
    private boolean placedByPlayer;
    private int growthTicksRemaining;
    private boolean duplicationActive;
    private int duplicationCursor;
    private int duplicationCooldown;
    private boolean lockedUntilSourceRemoved;
    private BlockPos sourcePos;
    private BlockState sourceState;
    private final Set<BlockPos> observedSources = new HashSet<>();
    private boolean sourceScanInitialized;

    public DuplicatorLogBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DUPLICATOR_LOG, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DuplicatorLogBlockEntity blockEntity) {
        if (!blockEntity.isController(level, pos)) {
            return;
        }

        blockEntity.tickGrowth(level, pos, state);
        blockEntity.tickDuplication(level, pos);
        blockEntity.tickUnlock(level);
        blockEntity.tickSourceDetection(level, pos);
    }

    public void markPlacedByPlayer() {
        this.placedByPlayer = true;
        this.growthTicksRemaining = DuplicatorTreeStructure.GROWTH_TICKS;
        this.setChanged();
    }

    public boolean tryStartDuplication(BlockPos placedPos, BlockState placedState) {
        if (this.level == null) {
            return false;
        }

        if (this.duplicationActive || this.lockedUntilSourceRemoved) {
            return false;
        }

        if (!DuplicatorTreeStructure.isCompleteTree(this.level, this.worldPosition, ModBlocks.DUPLICATOR_LOG, ModBlocks.DUPLICATOR_LEAVES)) {
            return false;
        }

        if (!DuplicatorTreeStructure.isValidSourceState(placedState, ModBlocks.DUPLICATOR_LOG, ModBlocks.DUPLICATOR_LEAVES)) {
            return false;
        }

        this.duplicationActive = true;
        this.duplicationCursor = 0;
        this.duplicationCooldown = DuplicatorTreeStructure.DUPLICATION_STEP_TICKS;
        this.sourcePos = placedPos.immutable();
        this.sourceState = placedState;
        this.setChanged();
        return true;
    }

    public static BlockPos findController(Level level, BlockPos startPos) {
        BlockPos cursor = startPos;

        while (level.getBlockState(cursor.below()).is(ModBlocks.DUPLICATOR_LOG)) {
            cursor = cursor.below();
        }

        return cursor;
    }

    private boolean isController(Level level, BlockPos pos) {
        return !level.getBlockState(pos.below()).is(ModBlocks.DUPLICATOR_LOG);
    }

    private void tickGrowth(Level level, BlockPos pos, BlockState state) {
        if (!this.placedByPlayer) {
            return;
        }

        if (DuplicatorTreeStructure.isCompleteTree(level, pos, ModBlocks.DUPLICATOR_LOG, ModBlocks.DUPLICATOR_LEAVES)) {
            this.placedByPlayer = false;
            this.growthTicksRemaining = 0;
            this.setChanged();
            return;
        }

        if (state.getValue(RotatedPillarBlock.AXIS) != Direction.Axis.Y) {
            return;
        }

        if (!DuplicatorTreeStructure.canGrowPlacedTree(level, pos, ModBlocks.DUPLICATOR_LOG, ModBlocks.DUPLICATOR_LEAVES)) {
            return;
        }

        if (this.growthTicksRemaining > 0) {
            this.growthTicksRemaining--;
            if (this.growthTicksRemaining % 20 == 0) {
                this.setChanged();
            }
            return;
        }

        DuplicatorTreeStructure.placeTree(
            level,
            pos,
            state,
            ModBlocks.DUPLICATOR_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.DISTANCE, Integer.valueOf(1))
                .setValue(LeavesBlock.PERSISTENT, Boolean.FALSE)
        );
        this.placedByPlayer = false;
        this.setChanged();
    }

    private void tickDuplication(Level level, BlockPos controllerPos) {
        if (!this.duplicationActive || this.sourcePos == null || this.sourceState == null) {
            return;
        }

        if (this.duplicationCooldown > 0) {
            this.duplicationCooldown--;
            if (this.duplicationCooldown % 20 == 0) {
                this.setChanged();
            }
            return;
        }

        List<BlockPos> targets = DuplicatorTreeStructure.buildDuplicationTargets(controllerPos, this.sourcePos);
        while (this.duplicationCursor < targets.size()) {
            BlockPos targetPos = targets.get(this.duplicationCursor++);
            if (!DuplicatorTreeStructure.canPlaceDuplicate(level, targetPos, this.sourceState, this.sourcePos, ModBlocks.DUPLICATOR_LOG, ModBlocks.DUPLICATOR_LEAVES)) {
                continue;
            }

            level.setBlock(targetPos, this.sourceState, 3);
            this.duplicationCooldown = DuplicatorTreeStructure.DUPLICATION_STEP_TICKS;
            this.setChanged();
            return;
        }

        this.duplicationActive = false;
        this.lockedUntilSourceRemoved = true;
        this.setChanged();
    }

    private void tickUnlock(Level level) {
        if (!this.lockedUntilSourceRemoved || this.sourcePos == null || this.sourceState == null) {
            return;
        }

        BlockState currentState = level.getBlockState(this.sourcePos);
        if (currentState.equals(this.sourceState)) {
            return;
        }

        this.lockedUntilSourceRemoved = false;
        this.sourcePos = null;
        this.sourceState = null;
        this.duplicationCursor = 0;
        this.duplicationCooldown = 0;
        this.setChanged();
    }

    private void tickSourceDetection(Level level, BlockPos controllerPos) {
        Set<BlockPos> currentSources = new HashSet<>();
        List<BlockPos> candidates = DuplicatorTreeStructure.buildSourceCandidates(controllerPos);
        boolean canStart = this.sourceScanInitialized
            && !this.placedByPlayer
            && !this.duplicationActive
            && !this.lockedUntilSourceRemoved
            && DuplicatorTreeStructure.isCompleteTree(level, controllerPos, ModBlocks.DUPLICATOR_LOG, ModBlocks.DUPLICATOR_LEAVES);

        for (BlockPos candidatePos : candidates) {
            BlockState candidateState = level.getBlockState(candidatePos);
            if (!DuplicatorTreeStructure.isValidSourceState(candidateState, ModBlocks.DUPLICATOR_LOG, ModBlocks.DUPLICATOR_LEAVES)) {
                continue;
            }

            BlockPos immutablePos = candidatePos.immutable();
            currentSources.add(immutablePos);
            if (canStart && !this.observedSources.contains(immutablePos) && this.tryStartDuplication(immutablePos, candidateState)) {
                break;
            }
        }

        this.observedSources.clear();
        this.observedSources.addAll(currentSources);
        this.sourceScanInitialized = true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("PlacedByPlayer", this.placedByPlayer);
        output.putInt("GrowthTicksRemaining", this.growthTicksRemaining);
        output.putBoolean("DuplicationActive", this.duplicationActive);
        output.putInt("DuplicationCursor", this.duplicationCursor);
        output.putInt("DuplicationCooldown", this.duplicationCooldown);
        output.putBoolean("LockedUntilSourceRemoved", this.lockedUntilSourceRemoved);
        output.storeNullable("SourcePos", BlockPos.CODEC, this.sourcePos);
        output.storeNullable("SourceState", BlockState.CODEC, this.sourceState);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.placedByPlayer = input.getBooleanOr("PlacedByPlayer", false);
        this.growthTicksRemaining = input.getIntOr("GrowthTicksRemaining", 0);
        this.duplicationActive = input.getBooleanOr("DuplicationActive", false);
        this.duplicationCursor = input.getIntOr("DuplicationCursor", 0);
        this.duplicationCooldown = input.getIntOr("DuplicationCooldown", 0);
        this.lockedUntilSourceRemoved = input.getBooleanOr("LockedUntilSourceRemoved", false);
        this.sourcePos = input.read("SourcePos", BlockPos.CODEC).orElse(null);
        this.sourceState = input.read("SourceState", BlockState.CODEC).orElse(null);
    }
}
