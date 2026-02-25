package com.apparati.apparati.content;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerApparatiAssembler extends Container {
    public final TileEntityApparatiAssembler te;

    public ContainerApparatiAssembler(InventoryPlayer playerInv, TileEntityApparatiAssembler te) {
        this.te = te;
        setupSlots();
        addPlayerSlots(playerInv);
    }

    private void setupSlots() {
        int activeTab = te.getActiveTab();
        System.out.println("DEBUG: Setting up slots for tab " + activeTab);

        // Crafting Slots: 0-8
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                final int slotIdx = j + i * 3;
                this.addSlotToContainer(new SlotItemHandler(te.craftingInv, slotIdx, activeTab == 0 ? 30 + j * 18 : -1000, 17 + i * 18) {
                    @Override
                    public void onSlotChanged() {
                        super.onSlotChanged();
                        if (te.getActiveTab() == 0) onCraftMatrixChanged();
                    }
                });
            }
        }
        // Crafting/Assembly Result: 9
        this.addSlotToContainer(new SlotItemHandler(te.craftingResult, 0, (activeTab == 0 || activeTab == 1) ? 124 : -1000, 35) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
                System.out.println("DEBUG: Taking result from tab " + te.getActiveTab());
                if (te.getActiveTab() == 0) {
                    for (int i = 0; i < te.craftingInv.getSlots(); i++) {
                        te.craftingInv.extractItem(i, 1, false);
                    }
                    onCraftMatrixChanged();
                } else if (te.getActiveTab() == 1) {
                    for (int i = 0; i < te.assemblyInv.getSlots(); i++) {
                        te.assemblyInv.extractItem(i, 1, false);
                    }
                    onAssemblyMatrixChanged();
                }
                return super.onTake(playerIn, stack);
            }
        });

        // Assembly Slots: 10-14
        this.addSlotToContainer(new PartSlot(te.assemblyInv, 0, activeTab == 1 ? 48 : -1000, 17, ApparatiPartItem.PartType.HEAD_REDSTONE_ANTENNAE) {
            @Override public void onSlotChanged() { super.onSlotChanged(); onAssemblyMatrixChanged(); }
        });
        this.addSlotToContainer(new PartSlot(te.assemblyInv, 1, activeTab == 1 ? 48 : -1000, 35, ApparatiPartItem.PartType.CHASSIS_HOLLOW) {
            @Override public void onSlotChanged() { super.onSlotChanged(); onAssemblyMatrixChanged(); }
        });
        this.addSlotToContainer(new PartSlot(te.assemblyInv, 2, activeTab == 1 ? 48 : -1000, 53, ApparatiPartItem.PartType.TREADS_WHEELIE) {
            @Override public void onSlotChanged() { super.onSlotChanged(); onAssemblyMatrixChanged(); }
        });
        this.addSlotToContainer(new PartSlot(te.assemblyInv, 3, activeTab == 1 ? 30 : -1000, 35, ApparatiPartItem.PartType.ARM_HOLDER) {
            @Override public void onSlotChanged() { super.onSlotChanged(); onAssemblyMatrixChanged(); }
        });
        this.addSlotToContainer(new PartSlot(te.assemblyInv, 4, activeTab == 1 ? 66 : -1000, 35, ApparatiPartItem.PartType.ARM_HOLDER) {
            @Override public void onSlotChanged() { super.onSlotChanged(); onAssemblyMatrixChanged(); }
        });

        // Programming Slot: 15
        this.addSlotToContainer(new SlotItemHandler(te.programmingInv, 0, activeTab == 2 ? 80 : -1000, 35) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() == ModItems.CORE;
            }
        });
        
        if (activeTab == 0) onCraftMatrixChanged();
        if (activeTab == 1) onAssemblyMatrixChanged();
    }

    public void updateSlots() {
        int activeTab = te.getActiveTab();
        
        // Crafting Slots: 0-8
        for (int i = 0; i < 9; ++i) {
            Slot slot = this.inventorySlots.get(i);
            int row = i / 3;
            int col = i % 3;
            slot.xPos = activeTab == 0 ? 30 + col * 18 : -1000;
        }

        // Crafting/Assembly Result: 9
        this.inventorySlots.get(9).xPos = (activeTab == 0 || activeTab == 1) ? 124 : -1000;

        // Assembly Slots: 10-14
        this.inventorySlots.get(10).xPos = activeTab == 1 ? 48 : -1000; // Head
        this.inventorySlots.get(11).xPos = activeTab == 1 ? 48 : -1000; // Chassis
        this.inventorySlots.get(12).xPos = activeTab == 1 ? 48 : -1000; // Treads
        this.inventorySlots.get(13).xPos = activeTab == 1 ? 30 : -1000; // Arm L
        this.inventorySlots.get(14).xPos = activeTab == 1 ? 66 : -1000; // Arm R

        // Programming Slot: 15
        this.inventorySlots.get(15).xPos = activeTab == 2 ? 80 : -1000;

        // Recalculate recipes/assembly if needed
        if (activeTab == 0) onCraftMatrixChanged();
        else if (activeTab == 1) onAssemblyMatrixChanged();
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
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 16) { // Container slots (Crafting, Result, Assembly, Programming)
                if (!this.mergeItemStack(itemstack1, 16, 52, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } else { // Player inventory slots
                if (te.getActiveTab() == 1) { // Assembly Tab
                    if (itemstack1.getItem() instanceof ApparatiPartItem) {
                        ApparatiPartItem.PartType type = ((ApparatiPartItem) itemstack1.getItem()).getPartType();
                        int targetSlot = -1;
                        switch (type.getCategory()) {
                            case HEAD: targetSlot = 10; break;
                            case CHASSIS: targetSlot = 11; break;
                            case TREADS: targetSlot = 12; break;
                            case ARM: 
                                // Try left arm, then right arm
                                if (!this.inventorySlots.get(13).getHasStack()) targetSlot = 13;
                                else if (!this.inventorySlots.get(14).getHasStack()) targetSlot = 14;
                                else targetSlot = 13; // Default to first if full? Or check valid?
                                break;
                            default: break;
                        }
                        
                        if (targetSlot != -1) {
                            if (!this.mergeItemStack(itemstack1, targetSlot, targetSlot + 1, false)) {
                                // If specific slot fails (e.g. ARM), try the other arm slot if applicable
                                if (type.getCategory() == ApparatiPartItem.PartCategory.ARM && targetSlot == 13) {
                                     if (!this.mergeItemStack(itemstack1, 14, 15, false)) {
                                         return ItemStack.EMPTY;
                                     }
                                } else {
                                    return ItemStack.EMPTY;
                                }
                            }
                        }
                    }
                } else if (te.getActiveTab() == 2) { // Programming Tab
                    if (itemstack1.getItem() == ModItems.CORE) {
                        if (!this.mergeItemStack(itemstack1, 15, 16, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else if (te.getActiveTab() == 0) { // Crafting Tab
                     if (!this.mergeItemStack(itemstack1, 0, 9, false)) {
                         return ItemStack.EMPTY;
                     }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (!te.getWorld().isRemote) {
            if (te.getActiveTab() == 0) onCraftMatrixChanged();
            else if (te.getActiveTab() == 1) onAssemblyMatrixChanged();
        }
    }

    public void onCraftMatrixChanged() {
        if (te.getWorld() == null) return;
        InventoryCrafting craftMatrix = new InventoryCrafting(new Container() {
            @Override public boolean canInteractWith(EntityPlayer playerIn) { return false; }
        }, 3, 3);
        for (int i = 0; i < 9; i++) craftMatrix.setInventorySlotContents(i, te.craftingInv.getStackInSlot(i));

        ItemStack result = ItemStack.EMPTY;
        for (IRecipe recipe : net.minecraftforge.fml.common.registry.ForgeRegistries.RECIPES) {
            if (recipe instanceof AssemblerShapedRecipe && recipe.matches(craftMatrix, te.getWorld())) {
                result = recipe.getCraftingResult(craftMatrix);
                break;
            }
        }
        te.craftingResult.setStackInSlot(0, result);
    }

    public void onAssemblyMatrixChanged() {
        if (te.getWorld() == null) return;
        ItemStack head = te.assemblyInv.getStackInSlot(0);
        ItemStack chassis = te.assemblyInv.getStackInSlot(1);
        ItemStack treads = te.assemblyInv.getStackInSlot(2);
        ItemStack armL = te.assemblyInv.getStackInSlot(3);
        ItemStack armR = te.assemblyInv.getStackInSlot(4);

        System.out.println("DEBUG: Assembly update check. Chassis=" + !chassis.isEmpty() + ", armL=" + !armL.isEmpty() + ", armR=" + !armR.isEmpty());

        if (!chassis.isEmpty() && (!armL.isEmpty() || !armR.isEmpty())) {
            ItemStack result = new ItemStack(ModItems.APPARATI);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("ChassisType", ((ApparatiPartItem)chassis.getItem()).getPartType().ordinal());
            tag.setString("ChassisMaterial", chassis.hasTagCompound() ? chassis.getTagCompound().getString("Material") : "iron");
            tag.setInteger("ArmLeftType", !armL.isEmpty() ? ((ApparatiPartItem)armL.getItem()).getPartType().ordinal() : -1);
            tag.setString("ArmLeftMaterial", !armL.isEmpty() && armL.hasTagCompound() ? armL.getTagCompound().getString("Material") : "iron");
            tag.setInteger("ArmRightType", !armR.isEmpty() ? ((ApparatiPartItem)armR.getItem()).getPartType().ordinal() : -1);
            tag.setString("ArmRightMaterial", !armR.isEmpty() && armR.hasTagCompound() ? armR.getTagCompound().getString("Material") : "iron");
            tag.setInteger("HeadType", !head.isEmpty() ? ((ApparatiPartItem)head.getItem()).getPartType().ordinal() : -1);
            tag.setString("HeadMaterial", !head.isEmpty() && head.hasTagCompound() ? head.getTagCompound().getString("Material") : "iron");
            tag.setInteger("TreadsType", !treads.isEmpty() ? ((ApparatiPartItem)treads.getItem()).getPartType().ordinal() : ApparatiPartItem.PartType.TREADS_WHEELIE.ordinal());
            tag.setString("TreadsMaterial", !treads.isEmpty() && treads.hasTagCompound() ? treads.getTagCompound().getString("Material") : "iron");
            tag.setFloat("Health", 20.0F);
            result.setTagCompound(tag);
            System.out.println("DEBUG: Setting assembly result!");
            te.craftingResult.setStackInSlot(0, result);
        } else {
            te.craftingResult.setStackInSlot(0, ItemStack.EMPTY);
        }
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
                return required.getCategory() == ((ApparatiPartItem) stack.getItem()).getPartType().getCategory();
            }
            return false;
        }
    }
}
