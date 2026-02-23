package com.apparati.apparati.content.client;

import com.apparati.apparati.content.ApparatiEntity;
import com.apparati.apparati.content.ApparatiPartItem;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import java.util.HashMap;
import java.util.Map;

public class ApparatiRenderer extends GeoEntityRenderer<ApparatiEntity> {
    private final Map<GeoVertex, float[]> originalUVs = new HashMap<>();

    public ApparatiRenderer(RenderManager renderManager) {
        super(renderManager, new ApparatiModel());
        this.shadowSize = 0.5F;
    }

    @Override
    public void render(GeoModel model, ApparatiEntity animatable, float partialTicks, float red, float green, float blue, float alpha) {
        // Clear previous UVs
        originalUVs.clear();

        // Apply materials to each part of the model
        applyMaterialsToModel(model, animatable);

        GlStateManager.enableAlpha();
        super.render(model, animatable, partialTicks, red, green, blue, alpha);

        // Restore UVs to avoid permanent modification of the cached model
        restoreUVs();
    }

    private void restoreUVs() {
        ApparatiTextureHelper.restoreUVs(originalUVs);
    }

    private void applyMaterialsToModel(GeoModel model, ApparatiEntity animatable) {
        for (GeoBone bone : model.topLevelBones) {
            applyMaterialsToBone(bone, animatable);
        }
    }

    private void applyMaterialsToBone(GeoBone bone, ApparatiEntity animatable) {
        // Determine which part this bone belongs to and get its material
        String material = getMaterialForBone(bone.name, animatable);
        
        if (material != null) {
            // Use helper to resolve the block state from the material string
            net.minecraft.block.state.IBlockState state = ApparatiTextureHelper.getBlockStateFromMaterial(material);
            
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
            
            if (sprite != null) {
                applySpriteToBoneGeometry(bone, sprite);
            }
        }

        // Recursively apply to children
        for (GeoBone child : bone.childBones) {
            applyMaterialsToBone(child, animatable);
        }
    }

    private String getMaterialForBone(String boneName, ApparatiEntity animatable) {
        // Check which part category this bone belongs to
        for (ApparatiPartItem.PartType type : ApparatiPartItem.PartType.values()) {
            if (type.getBones() != null) {
                for (String partBone : type.getBones()) {
                    if (partBone.equals(boneName)) {
                        // Found the part type, now get the material from the entity based on category
                        switch (type.getCategory()) {
                            case HEAD:
                                return animatable.getDataManager().get(ApparatiEntity.HEAD_MATERIAL);
                            case ARM:
                                // Differentiate left vs right arm based on bone name suffix or specific bone mapping
                                if (boneName.contains("left")) {
                                    return animatable.getDataManager().get(ApparatiEntity.ARM_LEFT_MATERIAL);
                                } else if (boneName.contains("right")) {
                                    return animatable.getDataManager().get(ApparatiEntity.ARM_RIGHT_MATERIAL);
                                }
                                // Fallback or shared parts? Assuming arms are distinct enough.
                                return animatable.getDataManager().get(ApparatiEntity.ARM_LEFT_MATERIAL); // Default?
                            case CHASSIS:
                                return animatable.getDataManager().get(ApparatiEntity.CHASSIS_MATERIAL);
                            case TREADS:
                                return animatable.getDataManager().get(ApparatiEntity.TREADS_MATERIAL);
                            case CORE:
                                return "iron"; // Core usually doesn't change material visually in the same way?
                            default:
                                return "iron";
                        }
                    }
                }
            }
        }
        
        // Handle parent bones or structural bones that might inherit material or default to chassis/iron
        if (boneName.equals("body") || boneName.equals("neck")) return animatable.getDataManager().get(ApparatiEntity.CHASSIS_MATERIAL); // Or head?
        if (boneName.equals("whole")) return null; // Root bone, no geometry usually

        return null; // No specific material found, maybe don't change texture?
    }

    private void applySpriteToBoneGeometry(GeoBone bone, TextureAtlasSprite sprite) {
        ApparatiTextureHelper.applySpriteToBoneGeometry(bone, sprite, originalUVs);
    }
}
