package com.apparati.apparati.content;

import com.apparati.apparati.ApparatiMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == BlockApparatiAssembler.GUI_ID) {
            TileEntityApparatiAssembler te = (TileEntityApparatiAssembler) world.getTileEntity(new BlockPos(x, y, z));
            return new ContainerApparatiAssembler(player.inventory, te);
        } else if (ID == ApparatiEntity.GUI_ID_INVENTORY) {
            net.minecraft.entity.Entity entity = world.getEntityByID(x);
            if (entity instanceof ApparatiEntity) {
                return new ContainerApparatiInventory(player.inventory, ((ApparatiEntity) entity).getInventory(), (ApparatiEntity) entity, player);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == BlockApparatiAssembler.GUI_ID) {
            TileEntityApparatiAssembler te = (TileEntityApparatiAssembler) world.getTileEntity(new BlockPos(x, y, z));
            return new GuiApparatiAssembler(new ContainerApparatiAssembler(player.inventory, te), te);
        } else if (ID == ApparatiEntity.GUI_ID_INVENTORY) {
            net.minecraft.entity.Entity entity = world.getEntityByID(x);
            if (entity instanceof ApparatiEntity) {
                return new GuiApparatiInventory(player.inventory, ((ApparatiEntity) entity).getInventory(), (ApparatiEntity) entity);
            }
        }
        return null;
    }
}
