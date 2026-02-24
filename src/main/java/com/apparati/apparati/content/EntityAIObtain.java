package com.apparati.apparati.content;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class EntityAIObtain extends EntityAIBase {
    private final ApparatiEntity apparati;
    private EntityItem targetItem;
    private BlockPos targetBlock;
    private String targetRegistryName;

    public EntityAIObtain(ApparatiEntity apparati) {
        this.apparati = apparati;
        this.setMutexBits(3); // Movement
    }

    @Override
    public boolean shouldExecute() {
        // Clear error state if we are starting a new execution cycle or check
        // But maybe we only clear if we successfully find a target and have the tool?
        // Let's rely on updateTask to set error state.
        
        ItemStack core = apparati.getDataManager().get(ApparatiEntity.CORE_STACK);
        if (core.isEmpty() || !core.hasTagCompound()) return false;

        NBTTagList behaviors = core.getTagCompound().getTagList("Behaviors", Constants.NBT.TAG_STRING);
        for (int i = 0; i < behaviors.tagCount(); i++) {
            String behavior = behaviors.getStringTagAt(i);
            if (behavior.startsWith("obtain:")) {
                String[] parts = behavior.split(":");
                if (parts.length >= 3) {
                    targetRegistryName = parts[1] + ":" + parts[2];
                    return findTarget();
                }
            }
        }
        return false;
    }

    private boolean findTarget() {
        // 1. Look for EntityItem
        List<EntityItem> items = apparati.world.getEntitiesWithinAABB(EntityItem.class, apparati.getEntityBoundingBox().grow(16.0D));
        for (EntityItem item : items) {
            if (item.getItem().getItem().getRegistryName().toString().equals(targetRegistryName)) {
                this.targetItem = item;
                this.targetBlock = null;
                return true;
            }
        }

        // 2. Look for Block (simplified: 5x5x5 around bot)
        // Note: Real implementation would need a larger scan area and caching
        BlockPos pos = apparati.getPosition();
        for (int x = -5; x <= 5; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos check = pos.add(x, y, z);
                    IBlockState state = apparati.world.getBlockState(check);
                    if (state.getBlock().getRegistryName().toString().equals(targetRegistryName)) {
                        this.targetBlock = check;
                        this.targetItem = null;
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    @Override
    public void startExecuting() {
        apparati.clearErrorState();
        if (targetItem != null) {
            apparati.getNavigator().tryMoveToEntityLiving(targetItem, 1.0D);
        } else if (targetBlock != null) {
            // Check for tool requirements before moving
            IBlockState state = apparati.world.getBlockState(targetBlock);
            apparati.equipBestTool(state);
            ItemStack held = apparati.getHeldItemMainhand();
            
            // Check if tool is sufficient (simplified check: if block requires tool, held item must be effective)
            if (state.getMaterial().isToolNotRequired() || (!held.isEmpty() && held.canHarvestBlock(state))) {
                apparati.getNavigator().tryMoveToXYZ(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), 1.0D);
            } else {
                apparati.setErrorState("Need Tool");
                resetTask();
            }
        }
    }

    @Override
    public void updateTask() {
        // If in error state, stop doing anything else
        if (apparati.getDataManager().get(ApparatiEntity.ERROR_STATE)) {
            return;
        }

        if (targetItem != null) {
            if (!targetItem.isEntityAlive()) {
                resetTask();
                return;
            }
            if (apparati.getDistanceSq(targetItem) < 4.0D) {
                // Pickup logic
                ItemStack stack = targetItem.getItem().copy();
                ItemStack remaining = addToInventory(stack);
                
                if (remaining.isEmpty()) {
                    targetItem.setDead();
                } else {
                     targetItem.setItem(remaining);
                }
                resetTask();
            }
        } else if (targetBlock != null) {
            if (apparati.getDistanceSqToCenter(targetBlock) < 9.0D) { // Range 3 blocks
                IBlockState state = apparati.world.getBlockState(targetBlock);
                apparati.equipBestTool(state);
                
                // Double check tool
                ItemStack held = apparati.getHeldItemMainhand();
                if (!state.getMaterial().isToolNotRequired() && (held.isEmpty() || !held.canHarvestBlock(state))) {
                     apparati.setErrorState("Need Tool");
                     return;
                }

                // Break block
                apparati.world.destroyBlock(targetBlock, true); // True = drop items
                resetTask();
            }
        }
    }

    private ItemStack addToInventory(ItemStack stack) {
        net.minecraft.inventory.InventoryBasic inv = apparati.getInventory();
        // Skip slot 0 (Core)
        for (int i = 1; i < inv.getSizeInventory(); i++) {
            ItemStack slotStack = inv.getStackInSlot(i);
            if (slotStack.isEmpty()) {
                inv.setInventorySlotContents(i, stack.copy());
                stack.setCount(0);
                return ItemStack.EMPTY;
            } else if (slotStack.getItem() == stack.getItem() && slotStack.getMetadata() == stack.getMetadata() && ItemStack.areItemStackTagsEqual(slotStack, stack)) {
                int space = slotStack.getMaxStackSize() - slotStack.getCount();
                if (space > 0) {
                    int transfer = Math.min(space, stack.getCount());
                    slotStack.grow(transfer);
                    stack.shrink(transfer);
                    if (stack.isEmpty()) return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return (targetItem != null && targetItem.isEntityAlive()) || (targetBlock != null && !apparati.world.isAirBlock(targetBlock));
    }
    
    @Override
    public void resetTask() {
        targetItem = null;
        targetBlock = null;
        apparati.getNavigator().clearPath();
    }
}
