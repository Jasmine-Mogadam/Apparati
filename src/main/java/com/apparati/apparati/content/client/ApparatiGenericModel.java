package com.apparati.apparati.content.client;

import com.apparati.apparati.content.ApparatiPartItem;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.core.IAnimatable;

import javax.annotation.Nullable;

public class ApparatiGenericModel<T extends IAnimatable> extends AnimatedGeoModel<T> {
    @Override
    public ResourceLocation getModelLocation(T object) {
        return new ResourceLocation("apparati", "geo/apparati_item.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(@Nullable T object) {
        return net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(T animatable) {
        return new ResourceLocation("apparati", "animations/apparati.animation.json");
    }

    public void setBoneVisible(String name, boolean visible) {
        IBone bone = this.getAnimationProcessor().getBone(name);
        if (bone != null) {
            bone.setHidden(!visible);
        }
    }
}
