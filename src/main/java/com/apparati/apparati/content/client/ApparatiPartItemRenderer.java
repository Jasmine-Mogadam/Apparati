package com.apparati.apparati.content.client;

import com.apparati.apparati.content.ApparatiPartItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.geo.render.built.GeoModel;

@SideOnly(Side.CLIENT)
public class ApparatiPartItemRenderer extends GeoItemRendererBase<GeoItemRendererBase.DummyAnimatable> {
    public static final ApparatiPartItemRenderer INSTANCE = new ApparatiPartItemRenderer();
    
    private ApparatiItemModel model;
    private DummyAnimatable dummy;

    private ApparatiItemModel getModel() {
        if (this.model == null) {
            this.model = new ApparatiItemModel();
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
        if (!(stack.getItem() instanceof ApparatiPartItem)) return;
        ApparatiPartItem item = (ApparatiPartItem) stack.getItem();

        setupRenderState();
        
        // Scale up (move closer)
        GlStateManager.scale(1.5, 1.5, 1.5);
        
        // Rotation (isometric-ish)
        GlStateManager.rotate(30, 1, 0, 0);
        GlStateManager.rotate(-210, 0, 1, 0);

        // per-item type offsets
        switch (item.getPartType().getCategory()) {
            case HEAD:
                GlStateManager.translate(-0.15, -0.55, 0);
                break;
            case ARM:
                GlStateManager.translate(-0.25, -0.25, 0);
                break;
            case CHASSIS:
                GlStateManager.translate(0, -0.45, 0);
                break;
            case TREADS:
                GlStateManager.translate(0.1, -0.2, 0);
                break;
            default:
                break;
        }

        ApparatiItemModel model = getModel();
        model.setCurrentPart(item.getPartType());
        
        // Get material from NBT
        String material = "iron";
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Material")) {
            material = stack.getTagCompound().getString("Material");
        }
        model.setCurrentMaterial(material);
        
        // Manually update bone visibility because setLivingAnimations isn't always called for items
        model.updateVisibility();

        GeoModel geoModel = model.getModel(model.getModelLocation(null));
        
        // Apply block texture sprite logic
        net.minecraft.block.state.IBlockState state = ApparatiTextureHelper.getBlockStateFromMaterial(material);
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
        
        // Clear previous UVs
        originalUVs.clear();

        if (sprite != null) {
            applySpriteToModel(geoModel, sprite);
        }

        this.render(geoModel, getDummy(), partialTicks, 1.0f, 1.0f, 1.0f, 1.0f);

        // Restore UVs
        restoreUVs();

        tearDownRenderState();
    }

    @Override
    public software.bernie.geckolib3.model.AnimatedGeoModel<DummyAnimatable> getGeoModelProvider() {
        return this.getModel();
    }

    @Override
    public ResourceLocation getTextureLocation(DummyAnimatable instance) {
        return this.getModel().getTextureLocation(instance);
    }
}
