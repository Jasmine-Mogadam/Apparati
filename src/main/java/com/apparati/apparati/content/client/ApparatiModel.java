package com.apparati.apparati.content.client;

import com.apparati.apparati.content.ApparatiEntity;
import com.apparati.apparati.content.ApparatiPartItem;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

import java.util.List;

public class ApparatiModel extends AnimatedGeoModel<ApparatiEntity> {
    @Override
    public ResourceLocation getModelLocation(ApparatiEntity object) {
        return new ResourceLocation("apparati", "geo/apparati.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ApparatiEntity object) {
        return new ResourceLocation("apparati", "textures/entity/apparati.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ApparatiEntity animatable) {
        return new ResourceLocation("apparati", "animations/apparati.animation.json");
    }

    @Override
    public void setLivingAnimations(ApparatiEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);
        IBone headBone = this.getAnimationProcessor().getBone("head");
        
        if (headBone != null) {
            // headBone.setHidden(false); 
            // In a real implementation, you would toggle visibility of sub-bones based on entity state
            // For example:
            // IBone cameraLens = this.getAnimationProcessor().getBone("camera_lens");
            // if (cameraLens != null) cameraLens.setHidden(entity.getDataManager().get(ApparatiEntity.HEAD_TYPE) != ApparatiPartItem.PartType.HEAD_CAMERA_LENS.ordinal());
        }
    }
}
