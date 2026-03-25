package com.bluedev.blockduplicatortree.block.entity;

import com.bluedev.blockduplicatortree.registry.ModBlockEntities;
import com.bluedev.blockduplicatortree.registry.ModBlocks;
import com.bluedev.blockduplicatortree.util.DuplicatorTreeStructure;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class DuplicatorLogBlockEntity extends BlockEntity {
    private boolean placedByPlayer;
    private int growthTicksRemaining;
    private boolean duplicationActive;
    private int duplicationCursor;
    private int duplicationCooldown;
    private boolean lockedUntilSourceRemoved;
    @Nullable
    private BlockPos sourcePos;
    @Nullable
    private BlockState sourceState;

    public DuplicatorLogBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DUPLICATOR_LOG.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DuplicatorLogBlockEntity blockEntity) {
        if (!blockEntity.isController(level, pos)) {
            return;
        }

        blockEntity.tickGrowth(level, pos, state);
        blockEntity.tickDuplication(level, pos);
        blockEntity.tickUnlock(level);
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

        if (!DuplicatorTreeStructure.isCompleteTree(this.level, this.worldPosition, ModBlocks.DUPLICATOR_LOG.get(), ModBlocks.DUPLICATOR_LEAVES.get())) {
            return false;
        }

        if (!DuplicatorTreeStructure.isValidSourceState(placedState, ModBlocks.DUPLICATOR_LOG.get(), ModBlocks.DUPLICATOR_LEAVES.get())) {
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

        while (level.getBlockState(cursor.below()).is(ModBlocks.DUPLICATOR_LOG.get())) {
            cursor = cursor.below();
        }

        return cursor;
    }

    private boolean isController(Level level, BlockPos pos) {
        return !level.getBlockState(pos.below()).is(ModBlocks.DUPLICATOR_LOG.get());
    }

    private void tickGrowth(Level level, BlockPos pos, BlockState state) {
        if (!this.placedByPlayer) {
            return;
        }

        if (DuplicatorTreeStructure.isCompleteTree(level, pos, ModBlocks.DUPLICATOR_LOG.get(), ModBlocks.DUPLICATOR_LEAVES.get())) {
            this.placedByPlayer = false;
            this.growthTicksRemaining = 0;
            this.setChanged();
            return;
        }

        if (state.getValue(RotatedPillarBlock.AXIS) != Direction.Axis.Y) {
            return;
        }

        if (!DuplicatorTreeStructure.canGrowPlacedTree(level, pos, ModBlocks.DUPLICATOR_LOG.get(), ModBlocks.DUPLICATOR_LEAVES.get())) {
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
            ModBlocks.DUPLICATOR_LEAVES.get().defaultBlockState()
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
            if (!DuplicatorTreeStructure.canPlaceDuplicate(level, targetPos, this.sourceState, this.sourcePos, ModBlocks.DUPLICATOR_LOG.get(), ModBlocks.DUPLICATOR_LEAVES.get())) {
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("PlacedByPlayer", this.placedByPlayer);
        tag.putInt("GrowthTicksRemaining", this.growthTicksRemaining);
        tag.putBoolean("DuplicationActive", this.duplicationActive);
        tag.putInt("DuplicationCursor", this.duplicationCursor);
        tag.putInt("DuplicationCooldown", this.duplicationCooldown);
        tag.putBoolean("LockedUntilSourceRemoved", this.lockedUntilSourceRemoved);

        if (this.sourcePos != null) {
            tag.putLong("SourcePos", this.sourcePos.asLong());
        }
        if (this.sourceState != null) {
            tag.put("SourceState", NbtUtils.writeBlockState(this.sourceState));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.placedByPlayer = tag.getBoolean("PlacedByPlayer");
        this.growthTicksRemaining = tag.getInt("GrowthTicksRemaining");
        this.duplicationActive = tag.getBoolean("DuplicationActive");
        this.duplicationCursor = tag.getInt("DuplicationCursor");
        this.duplicationCooldown = tag.getInt("DuplicationCooldown");
        this.lockedUntilSourceRemoved = tag.getBoolean("LockedUntilSourceRemoved");
        this.sourcePos = tag.contains("SourcePos") ? BlockPos.of(tag.getLong("SourcePos")) : null;
        this.sourceState = tag.contains("SourceState")
            ? NbtUtils.readBlockState(provider.lookupOrThrow(Registries.BLOCK), tag.getCompound("SourceState"))
            : null;
    }
}
