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
        this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC);
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
        if (stack.hasTagCompound()) {
            if (stack.getTagCompound().hasKey("Material")) {
                tooltip.add("Material: " + stack.getTagCompound().getString("Material"));
            }
            if (stack.getTagCompound().hasKey("Description")) {
                tooltip.add("Description: " + stack.getTagCompound().getString("Description"));
            }
        }
    }

    @Override
    public void onCreated(ItemStack stack, World worldIn, net.minecraft.entity.player.EntityPlayer playerIn) {
        // Materials are stored in NBT and will be used for entity textures later.
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
        }
        if (!stack.getTagCompound().hasKey("Material")) {
            // Defaulting to "iron" as it's the primary crafting component in recipes.
            stack.getTagCompound().setString("Material", "iron");
        }
    }

    public enum PartCategory {
        HEAD,
        ARM,
        CHASSIS,
        TREADS
    }

    public enum PartType {
        HEAD_REDSTONE_ANTENNAE(PartCategory.HEAD, "antennae", "flare"),
        HEAD_CAMERA_LENS(PartCategory.HEAD, "camera", "lid"),
        HEAD_MICROPHONE(PartCategory.HEAD, "microphone", "waffle rings"),
        ARM_HOLDER(PartCategory.ARM, "holder_left", "holder_right"),
        ARM_PLACER(PartCategory.ARM, "placer_left", "placer_right"),
        CHASSIS_HOLLOW(PartCategory.CHASSIS, "hollow"),
        CHASSIS_CHEST(PartCategory.CHASSIS, "chest"),
        CHASSIS_SOLID(PartCategory.CHASSIS, "solid"),
        TREADS_WHEELIE(PartCategory.TREADS, "wheelie"),
        TREADS_HOVER(PartCategory.TREADS, "hover");

        private final PartCategory category;
        private final String[] bones;

        PartType(PartCategory category, String... bones) {
            this.category = category;
            this.bones = bones;
        }

        public PartCategory getCategory() {
            return category;
        }

        public String[] getBones() {
            return bones;
        }
    }
}
