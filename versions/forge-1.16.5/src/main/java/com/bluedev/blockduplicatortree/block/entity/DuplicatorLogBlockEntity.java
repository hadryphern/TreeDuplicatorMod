package com.bluedev.blockduplicatortree.block.entity;

import com.bluedev.blockduplicatortree.registry.ModBlockEntities;
import com.bluedev.blockduplicatortree.registry.ModBlocks;
import com.bluedev.blockduplicatortree.util.DuplicatorTreeStructure;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class DuplicatorLogBlockEntity extends TileEntity implements ITickableTileEntity {
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

    public DuplicatorLogBlockEntity() {
        super(ModBlockEntities.DUPLICATOR_LOG.get());
    }

    @Override
    public void tick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }

        World level = this.level;
        BlockPos pos = this.worldPosition;
        if (!this.isController(level, pos)) {
            return;
        }

        this.tickGrowth(level, pos, this.getBlockState());
        this.tickDuplication(level, pos);
        this.tickUnlock(level);
    }

    public void markPlacedByPlayer() {
        this.placedByPlayer = true;
        this.growthTicksRemaining = DuplicatorTreeStructure.GROWTH_TICKS;
        this.setChanged();
    }

    public boolean tryStartDuplication(BlockPos placedPos, BlockState placedState) {
        if (this.level == null || this.duplicationActive || this.lockedUntilSourceRemoved) {
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
        this.sourcePos = new BlockPos(placedPos);
        this.sourceState = placedState;
        this.setChanged();
        return true;
    }

    public static BlockPos findController(World level, BlockPos startPos) {
        BlockPos cursor = startPos;

        while (level.getBlockState(cursor.below()).getBlock() == ModBlocks.DUPLICATOR_LOG.get()) {
            cursor = cursor.below();
        }

        return cursor;
    }

    private boolean isController(World level, BlockPos pos) {
        return level.getBlockState(pos.below()).getBlock() != ModBlocks.DUPLICATOR_LOG.get();
    }

    private void tickGrowth(World level, BlockPos pos, BlockState state) {
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

        BlockState leavesState = ModBlocks.DUPLICATOR_LEAVES.get().defaultBlockState()
            .setValue(LeavesBlock.DISTANCE, Integer.valueOf(1))
            .setValue(LeavesBlock.PERSISTENT, Boolean.FALSE);
        DuplicatorTreeStructure.placeTree(level, pos, state, leavesState);
        this.placedByPlayer = false;
        this.setChanged();
    }

    private void tickDuplication(World level, BlockPos controllerPos) {
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

    private void tickUnlock(World level) {
        if (!this.lockedUntilSourceRemoved || this.sourcePos == null || this.sourceState == null) {
            return;
        }

        if (level.getBlockState(this.sourcePos).equals(this.sourceState)) {
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
    public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        this.placedByPlayer = tag.getBoolean("PlacedByPlayer");
        this.growthTicksRemaining = tag.getInt("GrowthTicksRemaining");
        this.duplicationActive = tag.getBoolean("DuplicationActive");
        this.duplicationCursor = tag.getInt("DuplicationCursor");
        this.duplicationCooldown = tag.getInt("DuplicationCooldown");
        this.lockedUntilSourceRemoved = tag.getBoolean("LockedUntilSourceRemoved");
        this.sourcePos = tag.contains("SourcePos") ? BlockPos.of(tag.getLong("SourcePos")) : null;
        this.sourceState = tag.contains("SourceState") ? NBTUtil.readBlockState(tag.getCompound("SourceState")) : null;
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
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
            tag.put("SourceState", NBTUtil.writeBlockState(this.sourceState));
        }
        return tag;
    }
}
