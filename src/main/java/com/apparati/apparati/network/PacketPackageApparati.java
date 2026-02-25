package com.apparati.apparati.network;

import com.apparati.apparati.content.ApparatiEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketPackageApparati implements IMessage {
    private int entityId;

    public PacketPackageApparati() {}

    public PacketPackageApparati(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
    }

    public static class Handler implements IMessageHandler<PacketPackageApparati, IMessage> {
        @Override
        public IMessage onMessage(PacketPackageApparati message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                Entity entity = player.world.getEntityByID(message.entityId);
                if (entity instanceof ApparatiEntity) {
                    ApparatiEntity apparati = (ApparatiEntity) entity;
                    if (apparati.getDistanceSq(player) < 64) {
                        ItemStack stack = apparati.packageToItem();
                        if (player.inventory.addItemStackToInventory(stack)) {
                            apparati.setDead();
                        } else {
                            // If inventory full, drop at player feet
                            player.dropItem(stack, false);
                            apparati.setDead();
                        }
                    }
                }
            });
            return null;
        }
    }
}
