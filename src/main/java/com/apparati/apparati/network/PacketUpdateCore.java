package com.apparati.apparati.network;

import com.apparati.apparati.content.ContainerApparatiAssembler;
import com.apparati.apparati.content.ModItems;
import com.apparati.apparati.content.TileEntityApparatiAssembler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketUpdateCore implements IMessage {
    private NBTTagCompound data;

    public PacketUpdateCore() {}

    public PacketUpdateCore(NBTTagCompound data) {
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        data = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, data);
    }

    public static class Handler implements IMessageHandler<PacketUpdateCore, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateCore message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                Container container = player.openContainer;
                if (container instanceof ContainerApparatiAssembler) {
                    TileEntityApparatiAssembler te = ((ContainerApparatiAssembler) container).te;
                    
                    // Slot 15 is the programming slot
                    ItemStack coreStack = te.programmingInv.getStackInSlot(0);
                    
                    if (!coreStack.isEmpty() && coreStack.getItem() == ModItems.CORE) {
                        // Apply the NBT data to the core item
                        // We merge the incoming NBT with existing, or overwrite specific keys
                        // Here we assume the packet sends the complete "Behaviors" tag or similar
                        
                        NBTTagCompound tag = coreStack.hasTagCompound() ? coreStack.getTagCompound() : new NBTTagCompound();
                        
                        // Merge or set keys from message.data
                        for (String key : message.data.getKeySet()) {
                            tag.setTag(key, message.data.getTag(key));
                        }
                        
                        coreStack.setTagCompound(tag);
                        te.markDirty();
                    }
                }
            });
            return null;
        }
    }
}
