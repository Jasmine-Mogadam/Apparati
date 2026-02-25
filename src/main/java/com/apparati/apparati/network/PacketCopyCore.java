package com.apparati.apparati.network;

import com.apparati.apparati.content.ApparatiPartItem;
import com.apparati.apparati.content.ModItems;
import com.apparati.apparati.content.ContainerApparatiAssembler;
import com.apparati.apparati.content.TileEntityApparatiAssembler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PacketCopyCore implements IMessage {
    private List<Integer> targetSlots;

    public PacketCopyCore() {
        this.targetSlots = new ArrayList<>();
    }

    public PacketCopyCore(List<Integer> targetSlots) {
        this.targetSlots = targetSlots;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        targetSlots = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            targetSlots.add(buf.readInt());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(targetSlots.size());
        for (Integer slot : targetSlots) {
            buf.writeInt(slot);
        }
    }

    public static class Handler implements IMessageHandler<PacketCopyCore, IMessage> {
        @Override
        public IMessage onMessage(PacketCopyCore message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerApparatiAssembler) {
                    ContainerApparatiAssembler container = (ContainerApparatiAssembler) player.openContainer;
                    TileEntityApparatiAssembler te = container.te;
                    
                    ItemStack sourceStack = te.programmingInv.getStackInSlot(0);
                    if (sourceStack.isEmpty() || sourceStack.getItem() != ModItems.CORE) return;
                    
                    // Only process if it's a Core part (assuming part type check is handled by slot restrictions or item type)
                    // The programming slot 0 is restricted to cores in ContainerApparatiAssembler, usually.
                    
                    NBTTagCompound sourceTag = sourceStack.hasTagCompound() ? sourceStack.getTagCompound() : new NBTTagCompound();
                    
                    for (int slotId : message.targetSlots) {
                        // These are player inventory slot IDs. 
                        // ContainerApparatiAssembler adds player inventory slots starting at index 36 (hotbar) and 9 (main inv).
                        // Standard container slot mapping:
                        // 0-35: Container slots (if any? Assembler has its own slots)
                        // Actually, ContainerApparatiAssembler adds its own slots first.
                        // We need to map the slot ID correctly.
                        // Assuming the client sends the raw slot ID from the Container.
                        
                        if (slotId >= 0 && slotId < container.inventorySlots.size()) {
                            ItemStack targetStack = container.inventorySlots.get(slotId).getStack();
                            
                            if (!targetStack.isEmpty() && targetStack.getItem() == ModItems.CORE) {
                                copyCoreData(sourceStack, targetStack);
                            }
                        }
                    }
                }
            });
            return null;
        }

        private void copyCoreData(ItemStack source, ItemStack target) {
            NBTTagCompound sourceTag = source.hasTagCompound() ? source.getTagCompound() : new NBTTagCompound();
            NBTTagCompound targetTag = target.hasTagCompound() ? target.getTagCompound() : new NBTTagCompound();
            
            // Copy Name & Display
            if (sourceTag.hasKey("CoreName")) {
                targetTag.setString("CoreName", sourceTag.getString("CoreName"));
            }
            if (sourceTag.hasKey("display")) {
                targetTag.setTag("display", sourceTag.getCompoundTag("display").copy());
            }
            
            // Copy Glint
            if (sourceTag.hasKey("CoreGlint")) {
                targetTag.setBoolean("CoreGlint", sourceTag.getBoolean("CoreGlint"));
            }
            if (sourceTag.hasKey("ench")) {
                targetTag.setTag("ench", sourceTag.getTagList("ench", 10).copy());
            }
            
            // Determine Tiers
            int sourceTier = sourceTag.hasKey("Tier") ? sourceTag.getInteger("Tier") : 1;
            int targetTier = targetTag.hasKey("Tier") ? targetTag.getInteger("Tier") : 1;
            
            // Copy Behaviors
            // We copy up to min(sourceTier, targetTier) behaviors
            int limit = Math.min(sourceTier, targetTier);
            
            for (int i = 0; i < limit; i++) {
                String behaviorKey = "Behavior" + i;
                String configKey = "Config" + i;
                
                if (sourceTag.hasKey(behaviorKey)) {
                    targetTag.setString(behaviorKey, sourceTag.getString(behaviorKey));
                } else {
                    targetTag.removeTag(behaviorKey);
                }
                
                if (sourceTag.hasKey(configKey)) {
                    targetTag.setTag(configKey, sourceTag.getCompoundTag(configKey).copy());
                } else {
                    targetTag.removeTag(configKey);
                }
            }
            
            // Clear any extra behaviors if target had more than source or limit
            // Wait, if targetTier > sourceTier, we only copy sourceTier behaviors. The rest of target's behaviors should probably be cleared or kept?
            // "If the core is a lower tier it should just commit the first X behaviors it can store."
            // Implicitly, if target is higher tier, we probably just copy all source behaviors. The extra slots on target remain as they were or clear?
            // Usually "Copy" implies making it identical. I'll clear the rest.
            
            for (int i = limit; i < targetTier; i++) {
                 targetTag.removeTag("Behavior" + i);
                 targetTag.removeTag("Config" + i);
            }

            target.setTagCompound(targetTag);
        }
    }
}
