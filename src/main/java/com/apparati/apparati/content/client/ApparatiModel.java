package com.apparati.apparati.content.client;

import com.apparati.apparati.content.ApparatiEntity;
import com.apparati.apparati.content.ApparatiPartItem;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ApparatiModel extends ApparatiGenericModel<ApparatiEntity> {
    @Override
    public ResourceLocation getModelLocation(ApparatiEntity object) {
        return new ResourceLocation("apparati", "geo/apparati.geo.json");
    }

    @Override
    public void setLivingAnimations(ApparatiEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);

        Set<String> visibleBones = new HashSet<>();

        // Add visible bones based on active parts
        addVisibleBones(visibleBones, entity.getDataManager().get(ApparatiEntity.HEAD_TYPE));
        addVisibleBones(visibleBones, entity.getDataManager().get(ApparatiEntity.CHASSIS_TYPE));
        addVisibleBones(visibleBones, entity.getDataManager().get(ApparatiEntity.TREADS_TYPE));

        // Handle arms with side-specific filtering
        addVisibleArmBones(visibleBones, entity.getDataManager().get(ApparatiEntity.ARM_LEFT_TYPE), "_left");
        addVisibleArmBones(visibleBones, entity.getDataManager().get(ApparatiEntity.ARM_RIGHT_TYPE), "_right");

        // Apply visibility to all known part bones
        for (ApparatiPartItem.PartType type : ApparatiPartItem.PartType.values()) {
            if (type.getBones() != null) {
                for (String boneName : type.getBones()) {
                    setBoneVisible(boneName, visibleBones.contains(boneName));
                }
            }
        }

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

    private void addVisibleBones(Set<String> visibleBones, int partTypeIndex) {
        if (partTypeIndex < 0 || partTypeIndex >= ApparatiPartItem.PartType.values().length) return;
        ApparatiPartItem.PartType type = ApparatiPartItem.PartType.values()[partTypeIndex];
        if (type.getBones() != null) {
            Collections.addAll(visibleBones, type.getBones());
        }
    }

    private void addVisibleArmBones(Set<String> visibleBones, int partTypeIndex, String sideSuffix) {
        if (partTypeIndex < 0 || partTypeIndex >= ApparatiPartItem.PartType.values().length) return;
        ApparatiPartItem.PartType type = ApparatiPartItem.PartType.values()[partTypeIndex];
        if (type.getBones() != null) {
            for (String boneName : type.getBones()) {
                if (boneName.endsWith(sideSuffix)) {
                    visibleBones.add(boneName);
                }
            }
        }
    }
}
