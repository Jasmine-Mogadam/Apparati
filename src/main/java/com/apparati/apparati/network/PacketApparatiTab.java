package com.apparati.apparati.network;

import com.apparati.apparati.content.ContainerApparatiAssembler;
import com.apparati.apparati.content.TileEntityApparatiAssembler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketApparatiTab implements IMessage {
    private int tabId;

    public PacketApparatiTab() {}

    public PacketApparatiTab(int tabId) {
        this.tabId = tabId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        tabId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(tabId);
    }

    public static class Handler implements IMessageHandler<PacketApparatiTab, IMessage> {
        @Override
        public IMessage onMessage(PacketApparatiTab message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                Container container = player.openContainer;
                if (container instanceof ContainerApparatiAssembler) {
                    TileEntityApparatiAssembler te = ((ContainerApparatiAssembler) container).te;
                    te.setActiveTab(message.tabId);
                    
                    // Force container to refresh slots based on new tab
                    // We can achieve this by re-initializing the container slots on the server side
                    // However, ContainerApparatiAssembler.setupSlots() is private and called in constructor.
                    // We might need to expose a method to re-setup slots or handle slot visibility updates differently.
                    // For now, let's see if we can just re-open the container on the server side to refresh everything cleanly.
                    
                    // Actually, re-opening on server side might be cleanest if we don't want to modify Container logic too much.
                    // But standard way is to update slots. 
                    // Let's modify ContainerApparatiAssembler to have a public refreshSlots() method first.
                    // But since I can't modify that file easily in this step without reading it again and checking visibility...
                    // Wait, I already read it. setupSlots is private.
                    
                    // Alternative: The client re-opens the GUI. 
                    // If we update the TE on server, and then the client re-opens the GUI, the new Container on server will read the new tab from TE.
                    // So just updating TE here is enough IF the client also re-opens the GUI.
                    // But re-opening GUI is clunky.
                    
                    // Better: Update TE, then force container to update.
                    ((ContainerApparatiAssembler) container).updateSlots();
                }
            });
            return null;
        }
    }
}
