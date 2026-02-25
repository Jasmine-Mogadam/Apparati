package com.apparati.apparati.content;

import com.apparati.apparati.Constants;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ApparatiItem extends Item {
    public ApparatiItem() {
        this.setRegistryName("apparati");
        this.setUnlocalizedName(Constants.MOD_ID + ".apparati");
        this.setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        // Creative mode check: If server side, ensure we run logic.
        // Client side just returns SUCCESS to trigger animation/sound prediction.
        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        } else if (!player.canPlayerEdit(pos.offset(facing), facing, player.getHeldItem(hand))) {
            return EnumActionResult.FAIL;
        } else {
            ItemStack stack = player.getHeldItem(hand);
            BlockPos spawnPos = pos.offset(facing);
            
            ApparatiEntity entity = new ApparatiEntity(worldIn);
            // Center on block
            entity.setLocationAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, net.minecraft.util.math.MathHelper.wrapDegrees(worldIn.rand.nextFloat() * 360.0F), 0.0F);
            entity.rotationYawHead = entity.rotationYaw;
            entity.renderYawOffset = entity.rotationYaw;
            entity.onInitialSpawn(worldIn.getDifficultyForLocation(spawnPos), null);

            if (stack.hasTagCompound()) {
                NBTTagCompound tag = stack.getTagCompound();
                
                // Set parts and materials (Overwrite random initialization from onInitialSpawn)
                entity.getDataManager().set(ApparatiEntity.HEAD_TYPE, tag.getInteger("HeadType"));
                entity.getDataManager().set(ApparatiEntity.ARM_LEFT_TYPE, tag.getInteger("ArmLeftType"));
                entity.getDataManager().set(ApparatiEntity.ARM_RIGHT_TYPE, tag.getInteger("ArmRightType"));
                entity.getDataManager().set(ApparatiEntity.CHASSIS_TYPE, tag.getInteger("ChassisType"));
                entity.getDataManager().set(ApparatiEntity.TREADS_TYPE, tag.getInteger("TreadsType"));

                entity.getDataManager().set(ApparatiEntity.HEAD_MATERIAL, tag.getString("HeadMaterial"));
                entity.getDataManager().set(ApparatiEntity.ARM_LEFT_MATERIAL, tag.getString("ArmLeftMaterial"));
                entity.getDataManager().set(ApparatiEntity.ARM_RIGHT_MATERIAL, tag.getString("ArmRightMaterial"));
                entity.getDataManager().set(ApparatiEntity.CHASSIS_MATERIAL, tag.getString("ChassisMaterial"));
                entity.getDataManager().set(ApparatiEntity.TREADS_MATERIAL, tag.getString("TreadsMaterial"));

                // Restore Health
                if (tag.hasKey("Health")) {
                    entity.setHealth(tag.getFloat("Health"));
                }

                // Restore Inventory
                if (tag.hasKey("Inventory")) {
                    entity.readEntityFromNBT(tag.getCompoundTag("Inventory"));
                }
            }

            worldIn.spawnEntity(entity);
            entity.playLivingSound();

            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        }
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey("Health") && stack.getTagCompound().getFloat("Health") < 20.0F;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Health")) {
            float health = stack.getTagCompound().getFloat("Health");
            return 1.0 - (double)(health / 20.0F);
        }
        return 0.0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            tooltip.add(TextFormatting.GRAY + "Chassis: " + TextFormatting.WHITE + tag.getString("ChassisMaterial") + " " + ApparatiPartItem.PartType.values()[tag.getInteger("ChassisType")].name());
            
            float health = tag.hasKey("Health") ? tag.getFloat("Health") : 20.0F;
            tooltip.add(TextFormatting.GRAY + "Health: " + (health < 10 ? TextFormatting.RED : TextFormatting.GREEN) + String.format("%.1f", health) + "/20.0");
        }
    }
}
