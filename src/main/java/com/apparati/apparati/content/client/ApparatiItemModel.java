package com.apparati.apparati.content.client;

import com.apparati.apparati.content.ApparatiPartItem;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

import javax.annotation.Nullable;

public class ApparatiItemModel extends AnimatedGeoModel<ApparatiPartItemRenderer.DummyAnimatable> {
    private ApparatiPartItem.PartType currentPart = null;
    private String currentMaterial = "iron";

    public void setCurrentPart(ApparatiPartItem.PartType partType) {
        this.currentPart = partType;
    }

    public void setCurrentMaterial(String material) {
        this.currentMaterial = material;
    }

    @Override
    public ResourceLocation getModelLocation(ApparatiPartItemRenderer.DummyAnimatable object) {
        return new ResourceLocation("apparati", "geo/apparati_item.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(@Nullable ApparatiPartItemRenderer.DummyAnimatable object) {
        // Return the block texture map as we will be using sprites from blocks
        return net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ApparatiPartItemRenderer.DummyAnimatable animatable) {
        return new ResourceLocation("apparati", "animations/apparati.animation.json");
    }

    // Comprehensive list of all bones in the model to ensure complete reset
    private static final String[] ALL_BONES = {
        "whole", "body", "neck", "head", "camera", "lid", "antennae", "flare", 
        "microphone", "waffle rings", "chassis", "hollow", "chest", "solid", 
        "arm_left", "holder_left", "placer_left", "arm_right", "placer_right", 
        "holder_right", "treads", "wheelie", "hover"
    };

    @Override
    public void setLivingAnimations(ApparatiPartItemRenderer.DummyAnimatable entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);
        updateVisibility();
    }

    public void updateVisibility() {
        // Explicitly hide EVERYTHING first
        for (String bone : ALL_BONES) {
            setBoneVisible(bone, false);
        }

        // Always show the root
        setBoneVisible("whole", true);

        if (currentPart != null) {
            // Enable the necessary hierarchy for the current part
            enableHierarchyForPart(currentPart);

            // Enable the part's own bones
            if (currentPart.getBones() != null) {
                for (String boneName : currentPart.getBones()) {
                    setBoneVisible(boneName, true);
                }
            }
        }
    }

    private void enableHierarchyForPart(ApparatiPartItem.PartType partType) {
        // Logic to enable parent bones based on where the part attaches
        switch (partType.getCategory()) {
            case HEAD:
                // Attached to Head -> Neck -> Body
                setBoneVisible("body", true);
                setBoneVisible("neck", true);
                setBoneVisible("head", true);
                break;
            case ARM:
                // Only show left arm for generic item representation
                setBoneVisible("body", true);
                setBoneVisible("arm_left", true);
                break;
            case CHASSIS:
                // Attached to Chassis -> Body
                setBoneVisible("body", true);
                setBoneVisible("chassis", true);
                break;
            case TREADS:
                // Attached to Treads -> Whole
                setBoneVisible("treads", true);
                break;
        }
    }

    private void setBoneVisible(String name, boolean visible) {
        IBone bone = this.getAnimationProcessor().getBone(name);
        if (bone != null) {
            bone.setHidden(!visible);
        }
    }
}
