package com.apparati.apparati.content;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerApparatiAssembler extends Container {
    private final TileEntityApparatiAssembler te;

    public ContainerApparatiAssembler(InventoryPlayer playerInv, TileEntityApparatiAssembler te) {
        this.te = te;
        
        addPlayerSlots(playerInv);
        setupTabs();
    }

    private void setupTabs() {
        this.inventorySlots.removeIf(s -> !(s.inventory instanceof InventoryPlayer));

        int tab = te.getActiveTab();
        if (tab == 0) { // Crafting
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    this.addSlotToContainer(new SlotItemHandler(te.craftingInv, j + i * 3, 30 + j * 18, 17 + i * 18));
                }
            }
            this.addSlotToContainer(new SlotItemHandler(te.craftingResult, 0, 124, 35));
        } else if (tab == 1) { // Assembly
            // Cross pattern: 5 slots
            // top: head sensor (0), middle: chassis (1), bottom: treads (2), left: arm (3), right: arm (4)
            this.addSlotToContainer(new PartSlot(te.assemblyInv, 0, 80, 17, ApparatiPartItem.PartType.HEAD_REDSTONE_ANTENNAE)); // Simplified check
            this.addSlotToContainer(new PartSlot(te.assemblyInv, 1, 80, 35, ApparatiPartItem.PartType.CHASSIS_HOLLOW));
            this.addSlotToContainer(new PartSlot(te.assemblyInv, 2, 80, 53, ApparatiPartItem.PartType.TREADS_WHEELIE));
            this.addSlotToContainer(new PartSlot(te.assemblyInv, 3, 62, 35, ApparatiPartItem.PartType.ARM_HOLDER));
            this.addSlotToContainer(new PartSlot(te.assemblyInv, 4, 98, 35, ApparatiPartItem.PartType.ARM_HOLDER));
        } else if (tab == 2) { // Programming
            this.addSlotToContainer(new PartSlot(te.programmingInv, 0, 80, 35, ApparatiPartItem.PartType.CORE));
        }
    }

    public void updateSlots() {
        // This is tricky, might need to re-add slots or just move them off-screen
        // For simplicity, we'll just handle visibility in the GUI if possible,
        // but Container usually needs consistent slot counts.
        // Actually, many mods just have all slots present but move them based on tab.
    }

    private void addPlayerSlots(InventoryPlayer playerInv) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(playerInv, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(te.getPos().add(0.5, 0.5, 0.5)) <= 64;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        // Basic shift-click implementation
        return ItemStack.EMPTY;
    }

    private static class PartSlot extends SlotItemHandler {
        private final ApparatiPartItem.PartType required;

        public PartSlot(ItemStackHandler inventoryIn, int index, int xPosition, int yPosition, ApparatiPartItem.PartType required) {
            super(inventoryIn, index, xPosition, yPosition);
            this.required = required;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            if (stack.getItem() instanceof ApparatiPartItem) {
                ApparatiPartItem.PartType type = ((ApparatiPartItem) stack.getItem()).getPartType();
                // Check if it matches the general category
                if (required.name().startsWith("HEAD") && type.name().startsWith("HEAD")) return true;
                if (required.name().startsWith("CHASSIS") && type.name().startsWith("CHASSIS")) return true;
                if (required.name().startsWith("TREADS") && type.name().startsWith("TREADS")) return true;
                if (required.name().startsWith("ARM") && type.name().startsWith("ARM")) return true;
                if (required == ApparatiPartItem.PartType.CORE && type == ApparatiPartItem.PartType.CORE) return true;
            }
            return false;
        }
    }
}
