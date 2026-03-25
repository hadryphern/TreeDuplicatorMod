package com.bluedev.blockduplicatortree.block.entity;

import com.bluedev.blockduplicatortree.registry.ModBlocks;
import com.bluedev.blockduplicatortree.util.DuplicatorTreeStructure;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public final class DuplicatorLogTileEntity extends TileEntity implements ITickable {
    private boolean placedByPlayer;
    private int growthTicksRemaining;
    private boolean duplicationActive;
    private int duplicationCursor;
    private int duplicationCooldown;
    private boolean lockedUntilSourceRemoved;
    @Nullable
    private BlockPos sourcePos;
    @Nullable
    private IBlockState sourceState;

    @Override
    public void update() {
        if (this.world == null || this.world.isRemote) {
            return;
        }

        if (!this.isController(this.pos)) {
            return;
        }

        this.tickGrowth(this.pos, this.world.getBlockState(this.pos));
        this.tickDuplication(this.pos);
        this.tickUnlock();
    }

    public void markPlacedByPlayer() {
        this.placedByPlayer = true;
        this.growthTicksRemaining = DuplicatorTreeStructure.GROWTH_TICKS;
        this.markDirty();
    }

    public boolean tryStartDuplication(BlockPos placedPos, IBlockState placedState) {
        if (this.world == null || this.duplicationActive || this.lockedUntilSourceRemoved) {
            return false;
        }

        if (!DuplicatorTreeStructure.isCompleteTree(this.world, this.pos, ModBlocks.DUPLICATOR_LOG, Blocks.LEAVES)) {
            return false;
        }

        if (!DuplicatorTreeStructure.isValidSourceState(placedState, ModBlocks.DUPLICATOR_LOG, Blocks.LEAVES)) {
            return false;
        }

        this.duplicationActive = true;
        this.duplicationCursor = 0;
        this.duplicationCooldown = DuplicatorTreeStructure.DUPLICATION_STEP_TICKS;
        this.sourcePos = placedPos.toImmutable();
        this.sourceState = placedState;
        this.markDirty();
        return true;
    }

    public static BlockPos findController(net.minecraft.world.World world, BlockPos startPos) {
        BlockPos cursor = startPos;

        while (world.getBlockState(cursor.down()).getBlock() == ModBlocks.DUPLICATOR_LOG) {
            cursor = cursor.down();
        }

        return cursor;
    }

    private boolean isController(BlockPos pos) {
        return this.world.getBlockState(pos.down()).getBlock() != ModBlocks.DUPLICATOR_LOG;
    }

    private void tickGrowth(BlockPos pos, IBlockState state) {
        if (!this.placedByPlayer) {
            return;
        }

        if (DuplicatorTreeStructure.isCompleteTree(this.world, pos, ModBlocks.DUPLICATOR_LOG, Blocks.LEAVES)) {
            this.placedByPlayer = false;
            this.growthTicksRemaining = 0;
            this.markDirty();
            return;
        }

        if (state.getValue(BlockRotatedPillar.AXIS) != EnumFacing.Axis.Y) {
            return;
        }

        if (!DuplicatorTreeStructure.canGrowPlacedTree(this.world, pos, ModBlocks.DUPLICATOR_LOG, Blocks.LEAVES)) {
            return;
        }

        if (this.growthTicksRemaining > 0) {
            this.growthTicksRemaining--;
            if (this.growthTicksRemaining % 20 == 0) {
                this.markDirty();
            }
            return;
        }

        IBlockState leavesState = Blocks.LEAVES.getDefaultState()
            .withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK)
            .withProperty(BlockLeaves.CHECK_DECAY, Boolean.FALSE)
            .withProperty(BlockLeaves.DECAYABLE, Boolean.TRUE);
        DuplicatorTreeStructure.placeTree(this.world, pos, state, leavesState);
        this.placedByPlayer = false;
        this.markDirty();
    }

    private void tickDuplication(BlockPos controllerPos) {
        if (!this.duplicationActive || this.sourcePos == null || this.sourceState == null) {
            return;
        }

        if (this.duplicationCooldown > 0) {
            this.duplicationCooldown--;
            if (this.duplicationCooldown % 20 == 0) {
                this.markDirty();
            }
            return;
        }

        List<BlockPos> targets = DuplicatorTreeStructure.buildDuplicationTargets(controllerPos, this.sourcePos);
        while (this.duplicationCursor < targets.size()) {
            BlockPos targetPos = targets.get(this.duplicationCursor++);
            if (!DuplicatorTreeStructure.canPlaceDuplicate(this.world, targetPos, this.sourceState, this.sourcePos, ModBlocks.DUPLICATOR_LOG, Blocks.LEAVES)) {
                continue;
            }

            this.world.setBlockState(targetPos, this.sourceState, 3);
            this.duplicationCooldown = DuplicatorTreeStructure.DUPLICATION_STEP_TICKS;
            this.markDirty();
            return;
        }

        this.duplicationActive = false;
        this.lockedUntilSourceRemoved = true;
        this.markDirty();
    }

    private void tickUnlock() {
        if (!this.lockedUntilSourceRemoved || this.sourcePos == null || this.sourceState == null) {
            return;
        }

        if (this.world.getBlockState(this.sourcePos).equals(this.sourceState)) {
            return;
        }

        this.lockedUntilSourceRemoved = false;
        this.sourcePos = null;
        this.sourceState = null;
        this.duplicationCursor = 0;
        this.duplicationCooldown = 0;
        this.markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.placedByPlayer = compound.getBoolean("PlacedByPlayer");
        this.growthTicksRemaining = compound.getInteger("GrowthTicksRemaining");
        this.duplicationActive = compound.getBoolean("DuplicationActive");
        this.duplicationCursor = compound.getInteger("DuplicationCursor");
        this.duplicationCooldown = compound.getInteger("DuplicationCooldown");
        this.lockedUntilSourceRemoved = compound.getBoolean("LockedUntilSourceRemoved");
        this.sourcePos = compound.hasKey("SourcePos") ? BlockPos.fromLong(compound.getLong("SourcePos")) : null;
        this.sourceState = compound.hasKey("SourceState") ? NBTUtil.readBlockState(compound.getCompoundTag("SourceState")) : null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("PlacedByPlayer", this.placedByPlayer);
        compound.setInteger("GrowthTicksRemaining", this.growthTicksRemaining);
        compound.setBoolean("DuplicationActive", this.duplicationActive);
        compound.setInteger("DuplicationCursor", this.duplicationCursor);
        compound.setInteger("DuplicationCooldown", this.duplicationCooldown);
        compound.setBoolean("LockedUntilSourceRemoved", this.lockedUntilSourceRemoved);
        if (this.sourcePos != null) {
            compound.setLong("SourcePos", this.sourcePos.toLong());
        }
        if (this.sourceState != null) {
            compound.setTag("SourceState", NBTUtil.writeBlockState(new NBTTagCompound(), this.sourceState));
        }
        return compound;
    }
}
