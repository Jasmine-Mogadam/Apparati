package com.apparati.apparati.content;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerApparatiInventory extends Container {
    private final IInventory apparatiInventory;
    private final ApparatiEntity apparati;

    private int numRows = 0;

    public ContainerApparatiInventory(IInventory playerInventory, IInventory apparatiInventory, final ApparatiEntity apparati, EntityPlayer player) {
        this.apparatiInventory = apparatiInventory;
        this.apparati = apparati;
        this.apparati.numPlayersUsing++;
        this.numRows = (apparatiInventory.getSizeInventory() - 1 + 4) / 5; // -1 for Core, 5 cols
        apparatiInventory.openInventory(player);

        // Core Slot (Slot 0)
        this.addSlotToContainer(new Slot(apparatiInventory, 0, 8, 54) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() == ModItems.CORE;
            }
        });

        // Storage slots (1-26) - Position managed by scrollTo
        // We initialize them off-screen or at top
        for (int i = 0; i < 26; ++i) {
            this.addSlotToContainer(new Slot(apparatiInventory, 1 + i, -10000, -10000));
        }

        // Player Inventory - Standard offsets
        int playerInvY = 84;
        
        for (int m = 0; m < 3; ++m) {
            for (int n = 0; n < 9; ++n) {
                this.addSlotToContainer(new Slot(playerInventory, n + m * 9 + 9, 8 + n * 18, playerInvY + m * 18));
            }
        }

        // Player Hotbar
        for (int o = 0; o < 9; ++o) {
            this.addSlotToContainer(new Slot(playerInventory, o, 8 + o * 18, playerInvY + 58));
        }
        
        // Initial scroll position
        this.scrollTo(0.0f);
    }
    
    public void scrollTo(float pos) {
        int visibleRows = 3;
        int i = this.numRows - visibleRows;
        int startRow = (int)((double)(pos * (float)i) + 0.5D);

        if (startRow < 0) {
            startRow = 0;
        }

        for (int j = 0; j < 6; ++j) { // Max rows 6
            for (int k = 0; k < 5; ++k) { // 5 Cols
                int index = k + (j + startRow) * 5;
                // Mapping index 0-25 to storage slots
                if (index >= 0 && index < 26) {
                    // Slot index in inventorySlots list: 0 is Core, 1..26 are storage
                    // So get(1 + index)
                    Slot slot = this.inventorySlots.get(1 + index);
                    
                    if (j < visibleRows) {
                        // Visible
                        slot.xPos = 80 + k * 18;
                        slot.yPos = 18 + j * 18;
                    } else {
                        // Hidden
                        slot.xPos = -10000;
                        slot.yPos = -10000;
                    }
                }
            }
        }
    }
    
    public int getInventoryRows() {
        return this.numRows;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return this.apparatiInventory.isUsableByPlayer(playerIn) && this.apparati.isEntityAlive() && this.apparati.getDistance(playerIn) < 8.0F;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < this.apparatiInventory.getSizeInventory()) {
                if (!this.mergeItemStack(itemstack1, this.apparatiInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, this.apparatiInventory.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        this.apparatiInventory.closeInventory(playerIn);
        this.apparati.numPlayersUsing--;
    }
}
