package com.apparati.apparati.content.client;

import com.apparati.apparati.content.ApparatiPartItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public abstract class GeoItemRendererBase<T extends IAnimatable> extends TileEntityItemStackRenderer implements IGeoRenderer<T> {
    protected final Map<GeoVertex, float[]> originalUVs = new HashMap<>();

    @Override
    public abstract void renderByItem(ItemStack stack, float partialTicks);

    protected void applySpriteToModel(GeoModel model, TextureAtlasSprite sprite) {
        for (GeoBone bone : model.topLevelBones) {
            applySpriteToBone(bone, sprite);
        }
    }

    protected void applySpriteToBone(GeoBone bone, TextureAtlasSprite sprite) {
        ApparatiTextureHelper.applySpriteToBoneGeometry(bone, sprite, originalUVs);
        for (GeoBone child : bone.childBones) {
            applySpriteToBone(child, sprite);
        }
    }

    protected void restoreUVs() {
        ApparatiTextureHelper.restoreUVs(originalUVs);
    }

    protected void setupRenderState() {
        GlStateManager.pushMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(0.5, 0.5, 0.5);
    }

    protected void tearDownRenderState() {
        GlStateManager.popMatrix();
    }

    public static class DummyAnimatable implements IAnimatable {
        private final AnimationFactory factory = new AnimationFactory(this);
        @Override public void registerControllers(AnimationData data) {
            // Explicitly set deactivated animation
            data.addAnimationController(new software.bernie.geckolib3.core.controller.AnimationController<>(this, "controller", 0, event -> {
                return software.bernie.geckolib3.core.PlayState.STOP;
            }));
        }
        @Override public AnimationFactory getFactory() { return factory; }
    }
}
