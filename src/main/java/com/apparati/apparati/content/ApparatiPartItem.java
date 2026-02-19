package com.apparati.apparati.content;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ApparatiPartItem extends Item {
    private final PartType partType;
    private final float defaultStatModifier;

    public ApparatiPartItem(String name, PartType partType, float defaultStatModifier) {
        this.setRegistryName(name);
        this.setUnlocalizedName(name);
        this.partType = partType;
        this.defaultStatModifier = defaultStatModifier;
        // this.setCreativeTab(CreativeTabs.MISC); // TODO: Set creative tab
    }

    public PartType getPartType() {
        return partType;
    }

    public float getStatModifier(ItemStack stack) {
        // Check for NBT modifier, else return default
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("StatModifier")) {
            return stack.getTagCompound().getFloat("StatModifier");
        }
        return defaultStatModifier;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("Part Type: " + partType.name());
        tooltip.add("Stat Modifier: " + getStatModifier(stack));
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Material")) {
            tooltip.add("Material: " + stack.getTagCompound().getString("Material"));
        }
    }

    public enum PartType {
        HEAD_REDSTONE_ANTENNAE,
        HEAD_CAMERA_LENS,
        HEAD_MICROPHONE,
        ARM_HOLDER,
        ARM_PLACER,
        CHASSIS_HOLLOW,
        CHASSIS_CHEST,
        CHASSIS_SOLID,
        TREADS_WHEELIE,
        CORE
    }
}
