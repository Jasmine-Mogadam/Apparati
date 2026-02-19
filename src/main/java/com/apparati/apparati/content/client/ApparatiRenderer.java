package com.apparati.apparati.content.client;

import com.apparati.apparati.content.ApparatiEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class ApparatiRenderer extends GeoEntityRenderer<ApparatiEntity> {
    public ApparatiRenderer(RenderManager renderManager) {
        super(renderManager, new ApparatiModel());
        this.shadowSize = 0.5F;
    }
}
