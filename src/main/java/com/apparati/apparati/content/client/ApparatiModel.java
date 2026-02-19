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
        // Return texture based on entity material if possible
        // We use the chassis material as the primary material for the texture selection
        String material = object.getDataManager().get(ApparatiEntity.CHASSIS_MATERIAL);
        if (material == null || material.isEmpty() || material.equals("iron")) {
            return new ResourceLocation("apparati", "textures/entity/apparati.png");
        }
        return new ResourceLocation("apparati", "textures/entity/apparati_" + material + ".png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ApparatiEntity animatable) {
        return new ResourceLocation("apparati", "animations/apparati.animation.json");
    }

    @Override
    public void setLivingAnimations(ApparatiEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);

        // Hide all part-specific bones first
        for (ApparatiPartItem.PartType type : ApparatiPartItem.PartType.values()) {
            for (String boneName : type.getBones()) {
                setBoneVisible(boneName, false);
            }
        }

        // Show bones for the current parts
        showBonesFor(entity.getDataManager().get(ApparatiEntity.HEAD_TYPE));
        showBonesFor(entity.getDataManager().get(ApparatiEntity.CHASSIS_TYPE));
        showBonesFor(entity.getDataManager().get(ApparatiEntity.TREADS_TYPE));

        // Handle arms individually to support left/right bone filtering
        showArmBones(entity.getDataManager().get(ApparatiEntity.ARM_LEFT_TYPE), "_left");
        showArmBones(entity.getDataManager().get(ApparatiEntity.ARM_RIGHT_TYPE), "_right");

        // Head and neck look-at logic
        EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
        IBone head = this.getAnimationProcessor().getBone("head");
        IBone neck = this.getAnimationProcessor().getBone("neck");

        if (head != null) {
            head.setRotationX(extraData.headPitch * ((float) Math.PI / 180F));
            head.setRotationY(extraData.netHeadYaw * ((float) Math.PI / 180F));
        }
        if (neck != null) {
            neck.setRotationY(extraData.netHeadYaw * ((float) Math.PI / 360F)); // Neck follows half-way
        }
    }

    private void showBonesFor(int partTypeIndex) {
        if (partTypeIndex < 0 || partTypeIndex >= ApparatiPartItem.PartType.values().length) return;
        ApparatiPartItem.PartType type = ApparatiPartItem.PartType.values()[partTypeIndex];
        for (String boneName : type.getBones()) {
            setBoneVisible(boneName, true);
        }
    }

    private void showArmBones(int partTypeIndex, String sideSuffix) {
        if (partTypeIndex < 0 || partTypeIndex >= ApparatiPartItem.PartType.values().length) return;
        ApparatiPartItem.PartType type = ApparatiPartItem.PartType.values()[partTypeIndex];
        for (String boneName : type.getBones()) {
            if (boneName.endsWith(sideSuffix)) {
                setBoneVisible(boneName, true);
            }
        }
    }

    private void setBoneVisible(String name, boolean visible) {
        IBone bone = this.getAnimationProcessor().getBone(name);
        if (bone != null) {
            bone.setHidden(!visible);
        }
    }
}
