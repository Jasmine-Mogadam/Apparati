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
        if (ID == 0) {
            TileEntityApparatiAssembler te = (TileEntityApparatiAssembler) world.getTileEntity(new BlockPos(x, y, z));
            return new ContainerApparatiAssembler(player.inventory, te);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == 0) {
            TileEntityApparatiAssembler te = (TileEntityApparatiAssembler) world.getTileEntity(new BlockPos(x, y, z));
            return new GuiApparatiAssembler(new ContainerApparatiAssembler(player.inventory, te), te);
        }
        return null;
    }
}
