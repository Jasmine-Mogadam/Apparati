package com.apparati.apparati.content.client;

import com.apparati.apparati.content.ApparatiPartItem;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;

@SideOnly(Side.CLIENT)
public class ApparatiPartItemRenderer extends TileEntityItemStackRenderer implements IGeoRenderer<ApparatiPartItemRenderer.DummyAnimatable> {
    public static final ApparatiPartItemRenderer INSTANCE = new ApparatiPartItemRenderer();
    private static final ApparatiModel MODEL = new ApparatiModel();
    private static final DummyAnimatable DUMMY = new DummyAnimatable();

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        if (!(stack.getItem() instanceof ApparatiPartItem)) return;
        ApparatiPartItem item = (ApparatiPartItem) stack.getItem();

        GlStateManager.pushMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(0.5, 0.5, 0.5);
        GlStateManager.scale(0.5, 0.5, 0.5);

        MODEL.setCurrentPart(item.getPartType());
        GeoModel geoModel = MODEL.getModel(MODEL.getModelLocation(null));
        
        TextureAtlasSprite sprite = null;
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("BlockEntity")) {
            Block block = Block.getBlockFromName(stack.getTagCompound().getString("BlockEntity"));
            if (block != null) {
                sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(block.getStateFromMeta(stack.getTagCompound().getInteger("BlockMeta")));
            }
        } else {
            // Default to iron block if no block info is present (e.g. JEI)
            Block ironBlock = Block.getBlockFromName("minecraft:iron_block");
            if (ironBlock != null) {
                sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(ironBlock.getDefaultState());
            }
        }

        if (sprite == null) {
            // Ultimate fallback to avoid pink checkers if iron_block is somehow missing
            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if (sprite != null) {
            applySpriteToModel(geoModel, sprite);
        } else {
            Minecraft.getMinecraft().renderEngine.bindTexture(MODEL.getTextureLocation(null));
        }
        
        this.render(geoModel, DUMMY, partialTicks, 1.0f, 1.0f, 1.0f, 1.0f);

        GlStateManager.popMatrix();
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
                    // This is a rough tiling: map the vertex's texture coordinates 
                    // within the sprite's bounds.
                    // gecko vertices usually have UVs based on the entity texture size.
                    // We can try to normalize them or just map them directly if they are 0-16 range.
                    float u = vertex.textureU;
                    float v = vertex.textureV;
                    
                    // Simple wrap/tile logic: 
                    // Use modulo to keep UVs within a 0-1 range relative to the sprite
                    float normalizedU = (u % 16.0f) / 16.0f;
                    float normalizedV = (v % 16.0f) / 16.0f;
                    
                    vertex.textureU = sprite.getInterpolatedU(normalizedU * 16.0);
                    vertex.textureV = sprite.getInterpolatedV(normalizedV * 16.0);
                }
            }
        }
        for (GeoBone child : bone.childBones) {
            applySpriteToBone(child, sprite);
        }
    }

    @Override
    public software.bernie.geckolib3.model.AnimatedGeoModel<DummyAnimatable> getGeoModelProvider() {
        return null;
    }

    @Override
    public ResourceLocation getTextureLocation(DummyAnimatable instance) {
        return null;
    }

    public static class DummyAnimatable implements IAnimatable {
        private final AnimationFactory factory = new AnimationFactory(this);
        @Override public void registerControllers(AnimationData data) {}
        @Override public AnimationFactory getFactory() { return factory; }
    }
}
