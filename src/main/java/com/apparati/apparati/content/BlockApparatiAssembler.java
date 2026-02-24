package com.apparati.apparati.content;

import com.apparati.apparati.ApparatiMod;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockApparatiAssembler extends Block {
    public static final int GUI_ID = 0;

    public BlockApparatiAssembler() {
        super(Material.IRON);
        this.setRegistryName("apparati_assembler");
        this.setUnlocalizedName("apparati_assembler");
        this.setHardness(5.0F);
        this.setResistance(10.0F);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityApparatiAssembler();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) return true;
        playerIn.openGui(ApparatiMod.instance, GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
}
