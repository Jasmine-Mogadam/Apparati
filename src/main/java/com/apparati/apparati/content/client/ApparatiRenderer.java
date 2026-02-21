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

public class ApparatiRenderer extends GeoEntityRenderer<ApparatiEntity> {
    public ApparatiRenderer(RenderManager renderManager) {
        super(renderManager, new ApparatiModel());
        this.shadowSize = 0.5F;
    }

    @Override
    public void render(GeoModel model, ApparatiEntity animatable, float partialTicks, float red, float green, float blue, float alpha) {
        // Find the material and update the model's bone UVs before rendering
        String material = animatable.getDataManager().get(ApparatiEntity.CHASSIS_MATERIAL);
        String blockName = material.contains(":") ? material : "minecraft:" + material + "_block";
        Block block = Block.getBlockFromName(blockName);
        if (block == null) block = net.minecraft.init.Blocks.IRON_BLOCK;

        TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(block.getDefaultState());
        
        if (sprite != null) {
            applySpriteToModel(model, sprite);
        }

        GlStateManager.enableAlpha();
        super.render(model, animatable, partialTicks, red, green, blue, alpha);
    }

    private void applySpriteToModel(GeoModel model, TextureAtlasSprite sprite) {
        for (GeoBone bone : model.topLevelBones) {
            applySpriteToBone(bone, sprite);
        }
    }

    private void applySpriteToBone(GeoBone bone, TextureAtlasSprite sprite) {
        for (GeoCube cube : bone.childCubes) {
            for (GeoQuad quad : cube.quads) {
                for (GeoVertex vertex : quad.vertices) {
                    float u = vertex.textureU;
                    float v = vertex.textureV;
                    float normalizedU = (u % 16.0f) / 16.0f;
                    float normalizedV = (v % 16.0f) / 16.0f;
                    if (normalizedU < 0) normalizedU += 1.0f;
                    if (normalizedV < 0) normalizedV += 1.0f;
                    vertex.textureU = sprite.getInterpolatedU(normalizedU * 16.0);
                    vertex.textureV = sprite.getInterpolatedV(normalizedV * 16.0);
                }
            }
        }
        for (GeoBone child : bone.childBones) {
            applySpriteToBone(child, sprite);
        }
    }
}
