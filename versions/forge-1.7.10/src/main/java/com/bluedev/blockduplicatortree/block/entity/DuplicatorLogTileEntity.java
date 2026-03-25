package com.bluedev.blockduplicatortree.block.entity;

import com.bluedev.blockduplicatortree.BlockDuplicatorTreeMod;
import com.bluedev.blockduplicatortree.util.DuplicatorTreeStructure;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public final class DuplicatorLogTileEntity extends TileEntity {
    private boolean placedByPlayer;
    private int growthTicksRemaining;
    private boolean duplicationActive;
    private int duplicationCursor;
    private int duplicationCooldown;
    private boolean lockedUntilSourceRemoved;
    private int sourceX;
    private int sourceY;
    private int sourceZ;
    private int sourceMeta;
    private Block sourceBlock;
    private final Set<String> observedSources = new HashSet<String>();
    private boolean sourceScanInitialized;

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void updateEntity() {
        if (this.worldObj == null || this.worldObj.isRemote) {
            return;
        }

        if (!this.isController()) {
            return;
        }

        this.tickGrowth();
        this.tickDuplication();
        this.tickUnlock();
        this.tickSourceDetection();
    }

    public void markPlacedByPlayer() {
        this.placedByPlayer = true;
        this.growthTicksRemaining = DuplicatorTreeStructure.GROWTH_TICKS;
        this.markDirty();
    }

    private boolean tryStartDuplication(int x, int y, int z, Block block, int meta) {
        if (this.worldObj == null || this.duplicationActive || this.lockedUntilSourceRemoved) {
            return false;
        }

        if (!DuplicatorTreeStructure.isCompleteTree(this.worldObj, this.xCoord, this.yCoord, this.zCoord, BlockDuplicatorTreeMod.DUPLICATOR_LOG)) {
            return false;
        }

        if (!DuplicatorTreeStructure.isValidSourceBlock(block, meta, BlockDuplicatorTreeMod.DUPLICATOR_LOG)) {
            return false;
        }

        this.duplicationActive = true;
        this.duplicationCursor = 0;
        this.duplicationCooldown = DuplicatorTreeStructure.DUPLICATION_STEP_TICKS;
        this.sourceX = x;
        this.sourceY = y;
        this.sourceZ = z;
        this.sourceBlock = block;
        this.sourceMeta = meta;
        this.markDirty();
        return true;
    }

    private boolean isController() {
        return this.worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord) != BlockDuplicatorTreeMod.DUPLICATOR_LOG;
    }

    private void tickGrowth() {
        if (!this.placedByPlayer) {
            return;
        }

        if (DuplicatorTreeStructure.isCompleteTree(this.worldObj, this.xCoord, this.yCoord, this.zCoord, BlockDuplicatorTreeMod.DUPLICATOR_LOG)) {
            this.placedByPlayer = false;
            this.growthTicksRemaining = 0;
            this.markDirty();
            return;
        }

        if (!DuplicatorTreeStructure.canGrowPlacedTree(this.worldObj, this.xCoord, this.yCoord, this.zCoord, BlockDuplicatorTreeMod.DUPLICATOR_LOG)) {
            return;
        }

        if (this.growthTicksRemaining > 0) {
            this.growthTicksRemaining--;
            if (this.growthTicksRemaining % 20 == 0) {
                this.markDirty();
            }
            return;
        }

        DuplicatorTreeStructure.placeTree(this.worldObj, this.xCoord, this.yCoord, this.zCoord, BlockDuplicatorTreeMod.DUPLICATOR_LOG);
        this.placedByPlayer = false;
        this.markDirty();
    }

    private void tickDuplication() {
        if (!this.duplicationActive || this.sourceBlock == null) {
            return;
        }

        if (this.duplicationCooldown > 0) {
            this.duplicationCooldown--;
            if (this.duplicationCooldown % 20 == 0) {
                this.markDirty();
            }
            return;
        }

        List<int[]> targets = DuplicatorTreeStructure.buildDuplicationTargets(this.xCoord, this.sourceY, this.zCoord, this.sourceX, this.sourceZ);
        while (this.duplicationCursor < targets.size()) {
            int[] target = targets.get(this.duplicationCursor++);
            if (!DuplicatorTreeStructure.canPlaceDuplicate(this.worldObj, target[0], target[1], target[2], this.sourceBlock, this.sourceMeta, this.sourceX, this.sourceY, this.sourceZ, BlockDuplicatorTreeMod.DUPLICATOR_LOG)) {
                continue;
            }

            this.worldObj.setBlock(target[0], target[1], target[2], this.sourceBlock, this.sourceMeta, 3);
            this.duplicationCooldown = DuplicatorTreeStructure.DUPLICATION_STEP_TICKS;
            this.markDirty();
            return;
        }

        this.duplicationActive = false;
        this.lockedUntilSourceRemoved = true;
        this.markDirty();
    }

    private void tickUnlock() {
        if (!this.lockedUntilSourceRemoved || this.sourceBlock == null) {
            return;
        }

        if (this.worldObj.getBlock(this.sourceX, this.sourceY, this.sourceZ) == this.sourceBlock
            && this.worldObj.getBlockMetadata(this.sourceX, this.sourceY, this.sourceZ) == this.sourceMeta) {
            return;
        }

        this.lockedUntilSourceRemoved = false;
        this.sourceBlock = null;
        this.duplicationCursor = 0;
        this.duplicationCooldown = 0;
        this.markDirty();
    }

    private void tickSourceDetection() {
        Set<String> currentSources = new HashSet<String>();
        boolean canStart = this.sourceScanInitialized
            && !this.placedByPlayer
            && !this.duplicationActive
            && !this.lockedUntilSourceRemoved
            && DuplicatorTreeStructure.isCompleteTree(this.worldObj, this.xCoord, this.yCoord, this.zCoord, BlockDuplicatorTreeMod.DUPLICATOR_LOG);

        int[][] candidates = DuplicatorTreeStructure.buildSourceCandidates(this.xCoord, this.yCoord, this.zCoord);
        int index;
        for (index = 0; index < candidates.length; index++) {
            int[] candidate = candidates[index];
            Block block = this.worldObj.getBlock(candidate[0], candidate[1], candidate[2]);
            int meta = this.worldObj.getBlockMetadata(candidate[0], candidate[1], candidate[2]);
            if (!DuplicatorTreeStructure.isValidSourceBlock(block, meta, BlockDuplicatorTreeMod.DUPLICATOR_LOG)) {
                continue;
            }

            String key = key(candidate[0], candidate[1], candidate[2]);
            currentSources.add(key);
            if (canStart && !this.observedSources.contains(key) && this.tryStartDuplication(candidate[0], candidate[1], candidate[2], block, meta)) {
                break;
            }
        }

        this.observedSources.clear();
        this.observedSources.addAll(currentSources);
        this.sourceScanInitialized = true;
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
        this.sourceX = compound.getInteger("SourceX");
        this.sourceY = compound.getInteger("SourceY");
        this.sourceZ = compound.getInteger("SourceZ");
        this.sourceMeta = compound.getInteger("SourceMeta");
        this.sourceBlock = compound.hasKey("SourceBlock") ? Block.getBlockFromName(compound.getString("SourceBlock")) : null;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("PlacedByPlayer", this.placedByPlayer);
        compound.setInteger("GrowthTicksRemaining", this.growthTicksRemaining);
        compound.setBoolean("DuplicationActive", this.duplicationActive);
        compound.setInteger("DuplicationCursor", this.duplicationCursor);
        compound.setInteger("DuplicationCooldown", this.duplicationCooldown);
        compound.setBoolean("LockedUntilSourceRemoved", this.lockedUntilSourceRemoved);
        compound.setInteger("SourceX", this.sourceX);
        compound.setInteger("SourceY", this.sourceY);
        compound.setInteger("SourceZ", this.sourceZ);
        compound.setInteger("SourceMeta", this.sourceMeta);
        if (this.sourceBlock != null) {
            compound.setString("SourceBlock", String.valueOf(Block.blockRegistry.getNameForObject(this.sourceBlock)));
        }
    }

    private static String key(int x, int y, int z) {
        return x + "," + y + "," + z;
    }
}
