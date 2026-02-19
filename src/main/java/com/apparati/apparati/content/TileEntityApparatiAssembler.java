package com.apparati.apparati.content;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class TileEntityApparatiAssembler extends TileEntity implements ITickable {
    
    // 0: Crafting Tab (handled separately or via inventory)
    // 1: Assembly Tab (5 slots: Cross pattern)
    // 2: Programming Tab (1 slot)
    
    public final ItemStackHandler assemblyInv = new ItemStackHandler(5);
    public final ItemStackHandler programmingInv = new ItemStackHandler(1);
    public final ItemStackHandler craftingInv = new ItemStackHandler(9); // Standard crafting grid
    public final ItemStackHandler craftingResult = new ItemStackHandler(1);

    private int activeTab = 0;

    @Override
    public void update() {
        // Animation logic handled by TESR using world time
    }

    public int getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(int activeTab) {
        this.activeTab = activeTab;
        markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("AssemblyInv", assemblyInv.serializeNBT());
        compound.setTag("ProgrammingInv", programmingInv.serializeNBT());
        compound.setTag("CraftingInv", craftingInv.serializeNBT());
        compound.setInteger("ActiveTab", activeTab);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        assemblyInv.deserializeNBT(compound.getCompoundTag("AssemblyInv"));
        programmingInv.deserializeNBT(compound.getCompoundTag("ProgrammingInv"));
        craftingInv.deserializeNBT(compound.getCompoundTag("CraftingInv"));
        activeTab = compound.getInteger("ActiveTab");
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
}
