package com.apparati.apparati.content.client;

import com.apparati.apparati.content.ApparatiEntity;
import com.apparati.apparati.content.ApparatiItem;
import com.apparati.apparati.content.ApparatiPartItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;

@SideOnly(Side.CLIENT)
public class ApparatiItemRenderer extends GeoItemRendererBase<GeoItemRendererBase.DummyAnimatable> {
    public static final ApparatiItemRenderer INSTANCE = new ApparatiItemRenderer();
    
    private ApparatiGenericModel<DummyAnimatable> model;
    private DummyAnimatable dummy;

    private ApparatiGenericModel<DummyAnimatable> getModel() {
        if (this.model == null) {
            this.model = new ApparatiGenericModel<>();
        }
        return this.model;
    }

    private DummyAnimatable getDummy() {
        if (this.dummy == null) {
            this.dummy = new DummyAnimatable();
        }
        return this.dummy;
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        if (!(stack.getItem() instanceof ApparatiItem)) return;

        setupRenderState();
        
        // Scale and Rotate for full entity view
        GlStateManager.scale(0.8, 0.8, 0.8);
        GlStateManager.rotate(30, 1, 0, 0);
        GlStateManager.rotate(135, 0, 1, 0);
        GlStateManager.translate(0, -0.5, 0);

        ApparatiGenericModel<DummyAnimatable> modelProvider = getModel();
        GeoModel geoModel = modelProvider.getModel(modelProvider.getModelLocation(null));
        
        // Apply materials based on NBT
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            applyMaterialsToModel(geoModel, tag, modelProvider);
        } else {
            // Default iron look if no NBT
            applyDefaultMaterials(geoModel);
        }

        this.render(geoModel, getDummy(), partialTicks, 1.0f, 1.0f, 1.0f, 1.0f);

        restoreUVs();
        tearDownRenderState();
    }

    private void applyMaterialsToModel(GeoModel model, NBTTagCompound tag, ApparatiGenericModel<DummyAnimatable> modelProvider) {
        for (GeoBone bone : model.topLevelBones) {
            applyMaterialsToBone(bone, tag, modelProvider);
        }
    }

    private void applyMaterialsToBone(GeoBone bone, NBTTagCompound tag, ApparatiGenericModel<DummyAnimatable> modelProvider) {
        String material = getMaterialForBone(bone.name, tag);
        if (material != null) {
            net.minecraft.block.state.IBlockState state = ApparatiTextureHelper.getBlockStateFromMaterial(material);
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
            if (sprite != null) {
                applySpriteToBone(bone, sprite);
            }
        }
        
        // Visibility logic
        updateBoneVisibility(bone, tag, modelProvider);

        for (GeoBone child : bone.childBones) {
            applyMaterialsToBone(child, tag, modelProvider);
        }
    }

    private String getMaterialForBone(String boneName, NBTTagCompound tag) {
        if (boneName.contains("head") || boneName.contains("camera") || boneName.contains("microphone") || boneName.contains("antennae")) 
            return tag.getString("HeadMaterial");
        if (boneName.contains("left")) return tag.getString("ArmLeftMaterial");
        if (boneName.contains("right")) return tag.getString("ArmRightMaterial");
        if (boneName.contains("chassis") || boneName.contains("body") || boneName.contains("neck")) 
            return tag.getString("ChassisMaterial");
        if (boneName.contains("treads") || boneName.contains("wheelie")) 
            return tag.getString("TreadsMaterial");
        return "iron";
    }

    private void updateBoneVisibility(GeoBone bone, NBTTagCompound tag, ApparatiGenericModel<DummyAnimatable> modelProvider) {
        // Initial state: visible
        boolean visible = true;

        // Parts check
        int headType = tag.getInteger("HeadType");
        int armLeftType = tag.getInteger("ArmLeftType");
        int armRightType = tag.getInteger("ArmRightType");
        int chassisType = tag.getInteger("ChassisType");
        int treadsType = tag.getInteger("TreadsType");

        // Simple filtering: If it belongs to a part type that isn't selected, hide it.
        // This is a bit complex to do generic, but we can do a quick check against ALL bones.
        for (ApparatiPartItem.PartType type : ApparatiPartItem.PartType.values()) {
            if (type.getBones() != null) {
                for (String partBone : type.getBones()) {
                    if (partBone.equals(bone.name)) {
                        // This bone belongs to this part type.
                        // Check if this part type is active.
                        boolean active = false;
                        if (type.ordinal() == headType) active = true;
                        if (type.ordinal() == chassisType) active = true;
                        if (type.ordinal() == treadsType) active = true;
                        if (type.ordinal() == armLeftType && bone.name.endsWith("_left")) active = true;
                        if (type.ordinal() == armRightType && bone.name.endsWith("_right")) active = true;
                        
                        if (!active) visible = false;
                        break;
                    }
                }
            }
            if (!visible) break;
        }

        bone.setHidden(!visible);
    }

    private void applyDefaultMaterials(GeoModel model) {
        net.minecraft.block.state.IBlockState state = ApparatiTextureHelper.getBlockStateFromMaterial("iron");
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
        if (sprite != null) {
            applySpriteToModel(model, sprite);
        }
    }

    @Override
    public software.bernie.geckolib3.model.AnimatedGeoModel<DummyAnimatable> getGeoModelProvider() {
        return this.getModel();
    }

    @Override
    public ResourceLocation getTextureLocation(DummyAnimatable instance) {
        return net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
