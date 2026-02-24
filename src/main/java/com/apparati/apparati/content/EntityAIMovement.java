package com.apparati.apparati.content;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import java.util.ArrayList;
import java.util.List;

public class EntityAIMovement extends EntityAIBase {
    private final ApparatiEntity apparati;
    private List<BlockPos> patrolPoints = new ArrayList<>();
    private int currentPatrolIndex = 0;
    private Entity targetEntity;
    private Mode mode = Mode.NONE;

    private enum Mode {
        NONE, PATROL, NEAR
    }

    public EntityAIMovement(ApparatiEntity apparati) {
        this.apparati = apparati;
        this.setMutexBits(1); // Movement
    }

    @Override
    public boolean shouldExecute() {
        ItemStack core = apparati.getDataManager().get(ApparatiEntity.CORE_STACK);
        if (core.isEmpty() || !core.hasTagCompound()) return false;

        NBTTagList behaviors = core.getTagCompound().getTagList("Behaviors", Constants.NBT.TAG_STRING);
        for (int i = 0; i < behaviors.tagCount(); i++) {
            String behavior = behaviors.getStringTagAt(i);
            if (behavior.startsWith("movement:patrol")) {
                parsePatrol(behavior);
                if (!patrolPoints.isEmpty()) {
                    mode = Mode.PATROL;
                    return true;
                }
            } else if (behavior.startsWith("movement:near:player")) {
                // Find nearest player
                EntityPlayer player = apparati.world.getClosestPlayerToEntity(apparati, 16.0D);
                if (player != null) {
                    this.targetEntity = player;
                    mode = Mode.NEAR;
                    return true;
                }
            }
        }
        return false;
    }

    private void parsePatrol(String behavior) {
        patrolPoints.clear();
        String[] parts = behavior.split(":");
        // movement:patrol:x,y,z:x,y,z
        for (int i = 2; i < parts.length; i++) {
            String[] coords = parts[i].split(",");
            if (coords.length == 3) {
                try {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int z = Integer.parseInt(coords[2]);
                    patrolPoints.add(new BlockPos(x, y, z));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    @Override
    public void startExecuting() {
        if (mode == Mode.PATROL) {
            moveToNextPatrolPoint();
        } else if (mode == Mode.NEAR && targetEntity != null) {
            apparati.getNavigator().tryMoveToEntityLiving(targetEntity, 1.0D);
        }
    }

    @Override
    public void updateTask() {
        if (mode == Mode.PATROL) {
            if (patrolPoints.isEmpty()) return;
            BlockPos target = patrolPoints.get(currentPatrolIndex);
            if (apparati.getDistanceSqToCenter(target) < 2.0D || apparati.getNavigator().noPath()) {
                currentPatrolIndex = (currentPatrolIndex + 1) % patrolPoints.size();
                moveToNextPatrolPoint();
            }
        } else if (mode == Mode.NEAR) {
            if (targetEntity != null) {
                 if (apparati.getDistanceSq(targetEntity) > 16.0D) { // Keep close
                    apparati.getNavigator().tryMoveToEntityLiving(targetEntity, 1.0D);
                 }
            } else {
                resetTask();
            }
        }
    }

    private void moveToNextPatrolPoint() {
        if (patrolPoints.isEmpty()) return;
        BlockPos target = patrolPoints.get(currentPatrolIndex);
        apparati.getNavigator().tryMoveToXYZ(target.getX(), target.getY(), target.getZ(), 1.0D);
    }
    
    @Override
    public boolean shouldContinueExecuting() {
        if (mode == Mode.PATROL) return !patrolPoints.isEmpty();
        if (mode == Mode.NEAR) return targetEntity != null && targetEntity.isEntityAlive();
        return false;
    }
    
    @Override
    public void resetTask() {
        mode = Mode.NONE;
        targetEntity = null;
        patrolPoints.clear();
        apparati.getNavigator().clearPath();
    }
}
