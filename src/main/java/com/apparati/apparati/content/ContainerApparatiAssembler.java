package com.apparati.apparati.content;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerApparatiAssembler extends Container {
    private final TileEntityApparatiAssembler te;

    public ContainerApparatiAssembler(InventoryPlayer playerInv, TileEntityApparatiAssembler te) {
        this.te = te;
        
        setupSlots();
        addPlayerSlots(playerInv);
    }

    private void setupSlots() {
        // Assembler has several tabs with different slots
        // slots move off-screen (x = -1000) when their tab is not active.
        int activeTab = te.getActiveTab();

        // Crafting Slots: 0-8
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                final int slotIdx = j + i * 3;
                this.addSlotToContainer(new SlotItemHandler(te.craftingInv, slotIdx, activeTab == 0 ? 30 + j * 18 : -1000, 17 + i * 18) {
                    @Override
                    public void onSlotChanged() {
                        super.onSlotChanged();
                        onCraftMatrixChanged();
                    }
                });
            }
        }
        // Crafting Result: 9
        // Also used for Assembly result when activeTab == 1
        this.addSlotToContainer(new SlotItemHandler(te.craftingResult, 0, (activeTab == 0 || activeTab == 1) ? 124 : -1000, 35) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
                if (te.getActiveTab() == 0) {
                    for (int i = 0; i < te.craftingInv.getSlots(); i++) {
                        te.craftingInv.extractItem(i, 1, false);
                    }
                    onCraftMatrixChanged();
                } else if (te.getActiveTab() == 1) {
                    for (int i = 0; i < te.assemblyInv.getSlots(); i++) {
                        te.assemblyInv.extractItem(i, 1, false);
                    }
                    // Trigger assembly result update
                }
                return super.onTake(playerIn, stack);
            }
        });

        // Assembly Slots: 10-14 (Centered around 48, 35 - same as C tab cross)
        // Adjusting coordinates for better alignment with background
        // C tab slots are 30+j*18, 17+i*18. 
        // Cross is (1,0), (0,1), (1,1), (2,1), (1,2)
        // Indices: 1, 3, 4, 5, 7
        // (1,0) -> 30+1*18=48, 17+0*18=17
        // (0,1) -> 30+0*18=30, 17+1*18=35
        // (1,1) -> 30+1*18=48, 17+1*18=35
        // (2,1) -> 30+2*18=66, 17+1*18=35
        // (1,2) -> 30+1*18=48, 17+2*18=53
        this.addSlotToContainer(new PartSlot(te.assemblyInv, 0, activeTab == 1 ? 48 : -1000, 17, ApparatiPartItem.PartType.HEAD_REDSTONE_ANTENNAE));
        this.addSlotToContainer(new PartSlot(te.assemblyInv, 1, activeTab == 1 ? 48 : -1000, 35, ApparatiPartItem.PartType.CHASSIS_HOLLOW));
        this.addSlotToContainer(new PartSlot(te.assemblyInv, 2, activeTab == 1 ? 48 : -1000, 53, ApparatiPartItem.PartType.TREADS_WHEELIE));
        this.addSlotToContainer(new PartSlot(te.assemblyInv, 3, activeTab == 1 ? 30 : -1000, 35, ApparatiPartItem.PartType.ARM_HOLDER));
        this.addSlotToContainer(new PartSlot(te.assemblyInv, 4, activeTab == 1 ? 66 : -1000, 35, ApparatiPartItem.PartType.ARM_HOLDER));

        // Programming Slot: 15
        this.addSlotToContainer(new PartSlot(te.programmingInv, 0, activeTab == 2 ? 80 : -1000, 35, ApparatiPartItem.PartType.CORE));
        
        if (activeTab == 0) onCraftMatrixChanged();
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

    private java.util.List<IRecipe> matchingRecipes = new java.util.ArrayList<>();

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        
        // If there's more than 1 matching recipe, cycle through them every 2 seconds
        if (!te.getWorld().isRemote && matchingRecipes.size() > 1) {
            long time = te.getWorld().getTotalWorldTime();
            if (time % 40 == 0) { // Cycle every 2 seconds
                int index = (int) ((time / 40) % matchingRecipes.size());
                
                // Re-creates craftMatrix to get result on swap for consistency (use same items)
                // Use a dummy container to prevent recursion via onCraftMatrixChanged -> detectAndSendChanges
                InventoryCrafting craftMatrix = new InventoryCrafting(new Container() {
                    @Override
                    public boolean canInteractWith(EntityPlayer playerIn) {
                        return false;
                    }
                    @Override
                    public void onCraftMatrixChanged(net.minecraft.inventory.IInventory inventoryIn) {
                        // No-op to prevent recursion
                    }
                }, 3, 3);

                for (int i = 0; i < 9; i++) {
                    craftMatrix.setInventorySlotContents(i, te.craftingInv.getStackInSlot(i));
                }
                
                te.craftingResult.setStackInSlot(0, matchingRecipes.get(index).getCraftingResult(craftMatrix));
            }
        }
    }

    public void onCraftMatrixChanged() {
        if (te.getWorld() == null) return;
        
        InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
        for (int i = 0; i < 9; i++) {
            craftMatrix.setInventorySlotContents(i, te.craftingInv.getStackInSlot(i));
        }

        matchingRecipes.clear();
        ItemStack result = ItemStack.EMPTY;
        
        for (IRecipe recipe : net.minecraftforge.fml.common.registry.ForgeRegistries.RECIPES) {
            // Only allow custom recipes in the assembler
            if (recipe instanceof AssemblerShapedRecipe) {
                if (recipe.matches(craftMatrix, te.getWorld())) {
                    matchingRecipes.add(recipe);
                }
            }
        }
        
        // if there's a result, display it. 
        // If multiple matches, cycle through them in detectAndSendChanges() 
        // to ensure the same result is shown for the same input until it changes.
        if (!matchingRecipes.isEmpty()) {
            long time = te.getWorld().getTotalWorldTime();
            int index = (int) ((time / 40) % matchingRecipes.size());
            result = matchingRecipes.get(index).getCraftingResult(craftMatrix);
        }
        
        te.craftingResult.setStackInSlot(0, result);
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
                ApparatiPartItem.PartCategory requiredCat = required.getCategory();
                ApparatiPartItem.PartCategory stackCat = type.getCategory();
                
                return requiredCat == stackCat;
            }
            return false;
        }
    }
}
