package com.apparati.apparati.content;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.List;

public class ApparatiEntity extends EntityCreature implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    // Data Parameters for Parts (Storing ID corresponding to PartType ordinal)
    public static final DataParameter<Integer> HEAD_TYPE = EntityDataManager.createKey(ApparatiEntity.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> ARM_LEFT_TYPE = EntityDataManager.createKey(ApparatiEntity.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> ARM_RIGHT_TYPE = EntityDataManager.createKey(ApparatiEntity.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> CHASSIS_TYPE = EntityDataManager.createKey(ApparatiEntity.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> TREADS_TYPE = EntityDataManager.createKey(ApparatiEntity.class, DataSerializers.VARINT);

    // Data Parameters for Materials
    public static final DataParameter<String> HEAD_MATERIAL = EntityDataManager.createKey(ApparatiEntity.class, DataSerializers.STRING);
    public static final DataParameter<String> ARM_LEFT_MATERIAL = EntityDataManager.createKey(ApparatiEntity.class, DataSerializers.STRING);
    public static final DataParameter<String> ARM_RIGHT_MATERIAL = EntityDataManager.createKey(ApparatiEntity.class, DataSerializers.STRING);
    public static final DataParameter<String> CHASSIS_MATERIAL = EntityDataManager.createKey(ApparatiEntity.class, DataSerializers.STRING);
    public static final DataParameter<String> TREADS_MATERIAL = EntityDataManager.createKey(ApparatiEntity.class, DataSerializers.STRING);

    // Valid materials for randomization
    private static final String[] VALID_MATERIALS = {"iron", "gold", "copper", "lead", "silver"};

    // Internal Inventory for "Core" and "Storage"
    private final InventoryBasic inventory;

    public ApparatiEntity(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.0F);
        this.inventory = new InventoryBasic("ApparatiInventory", false, 27); // Standard chest size
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HEAD_TYPE, 0);
        this.dataManager.register(ARM_LEFT_TYPE, 0);
        this.dataManager.register(ARM_RIGHT_TYPE, 0);
        this.dataManager.register(CHASSIS_TYPE, 0);
        this.dataManager.register(TREADS_TYPE, 0);

        this.dataManager.register(HEAD_MATERIAL, "iron");
        this.dataManager.register(ARM_LEFT_MATERIAL, "iron");
        this.dataManager.register(ARM_RIGHT_MATERIAL, "iron");
        this.dataManager.register(CHASSIS_MATERIAL, "iron");
        this.dataManager.register(TREADS_MATERIAL, "iron");
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote) {
            // Only tick sensors every 20 ticks (1 second) to save performance
            if (this.ticksExisted % 20 == 0) {
                tickSensors();
            }
            tickArms();
            tickChassisStats();
        }
    }

    private void tickSensors() {
        int headTypeIndex = this.dataManager.get(HEAD_TYPE);
        if (headTypeIndex < 0 || headTypeIndex >= ApparatiPartItem.PartType.values().length) return;
        
        ApparatiPartItem.PartType head = ApparatiPartItem.PartType.values()[headTypeIndex];

        switch (head) {
            case HEAD_REDSTONE_ANTENNAE:
                // Check for redstone signals nearby
                // Scanning a 5x5x5 area is expensive, so we limit it to run infrequently (see onUpdate)
                int radius = 5;
                boolean powered = false;
                BlockPos pos = this.getPosition();
                
                // Optimized loop with break label
                searchLoop:
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            if (this.world.isBlockPowered(pos.add(x, y, z))) {
                                powered = true;
                                break searchLoop;
                            }
                        }
                    }
                }
                if (powered) {
                    // Logic for receiving signal (e.g. store in NBT or react)
                    // For now, let's just emit particles or set a flag
                }
                break;
            case HEAD_CAMERA_LENS:
                // Core has access to all objects within X blocks in a cone
                double range = 10.0D;
                List<Entity> entities = this.world.getEntitiesWithinAABB(Entity.class, this.getEntityBoundingBox().grow(range));
                Vec3d lookVec = this.getLook(1.0F);
                for (Entity entity : entities) {
                    if (entity == this) continue;
                    Vec3d entityVec = entity.getPositionVector().subtract(this.getPositionVector()).normalize();
                    double dot = lookVec.dotProduct(entityVec);
                    if (dot > 0.5) { // Within ~60 degrees cone
                        // Detect entity
                        this.setRevengeTarget(entity instanceof EntityCreature ? (EntityCreature) entity : null); // Placeholder reaction
                    }
                }
                break;
            case HEAD_MICROPHONE:
                // Tricky in 1.12 without mixins. We can scan for "noisy" entities nearby?
                // Or maybe just check for entities moving fast nearby?
                break;
            default:
                break;
        }
    }

    private void tickArms() {
        int leftArmIndex = this.dataManager.get(ARM_LEFT_TYPE);
        int rightArmIndex = this.dataManager.get(ARM_RIGHT_TYPE);

        if (isHolder(leftArmIndex) || isHolder(rightArmIndex)) {
            // Holder Logic
            // If we have a target, attack it is handled by AI, but we can ensure tool usage effectiveness
            // Additional interactions: Stick for buttons
            if (!this.world.isRemote && this.ticksExisted % 20 == 0) {
                 ItemStack held = this.getHeldItemMainhand();
                 if (!held.isEmpty() && held.getItem().getUnlocalizedName().contains("stick")) {
                     // Look for buttons
                     BlockPos pos = this.getPosition();
                     for (int x = -2; x <= 2; x++) {
                         for (int y = 0; y <= 2; y++) {
                             for (int z = -2; z <= 2; z++) {
                                 BlockPos target = pos.add(x, y, z);
                                 if (this.world.getBlockState(target).getBlock() instanceof net.minecraft.block.BlockButton) {
                                     // Simulate click (toggle state)
                                     // This is simplified; real interaction requires more checks
                                     this.world.getBlockState(target).getBlock().onBlockActivated(this.world, target, this.world.getBlockState(target), (EntityPlayer) null, EnumHand.MAIN_HAND, null, 0.5f, 0.5f, 0.5f);
                                 }
                             }
                         }
                     }
                 }
            }
        }
        
        if (isPlacer(leftArmIndex) || isPlacer(rightArmIndex)) {
             // Placer Logic
             // 1. Bonemeal usage
             if (!this.world.isRemote && this.ticksExisted % 40 == 0) {
                 // Check inventory for bonemeal or placeable blocks
                 // Simplified: Just use held item for now
                 ItemStack held = this.getHeldItemMainhand();
                 if (!held.isEmpty()) {
                     if (held.getItem() instanceof net.minecraft.item.ItemDye) { // Bonemeal
                         // Try to bonemeal surroundings
                          BlockPos pos = this.getPosition();
                          for (int x = -2; x <= 2; x++) {
                              for (int z = -2; z <= 2; z++) {
                                  BlockPos target = pos.add(x, -1, z); // Ground
                                  if (net.minecraft.item.ItemDye.applyBonemeal(held, this.world, target)) {
                                      if (this.world.isRemote) {
                                          this.world.playEvent(2005, target, 0);
                                      }
                                      break; // Use once per tick cycle
                                  }
                              }
                          }
                     } else if (held.getItem() instanceof net.minecraft.item.ItemBlock) {
                         // Place block below if air
                         BlockPos below = this.getPosition().down();
                         if (this.world.isAirBlock(below)) {
                             // Place it
                             // ((net.minecraft.item.ItemBlock) held.getItem()).placeBlockAt(...) // Complex method signature
                             // For simplicity in this implementation, we skip complex placement logic
                         }
                     }
                 }
             }
        }
    }

    private boolean isHolder(int index) {
        return index == ApparatiPartItem.PartType.ARM_HOLDER.ordinal();
    }
    
    private boolean isPlacer(int index) {
        return index == ApparatiPartItem.PartType.ARM_PLACER.ordinal();
    }

    private void tickChassisStats() {
        int chassisIndex = this.dataManager.get(CHASSIS_TYPE);
        if (chassisIndex < 0 || chassisIndex >= ApparatiPartItem.PartType.values().length) return;
        ApparatiPartItem.PartType chassis = ApparatiPartItem.PartType.values()[chassisIndex];

        double speedMod = 0;
        double healthMod = 0;

        switch (chassis) {
            case CHASSIS_HOLLOW:
                speedMod = 0.1D;
                healthMod = -5.0D;
                break;
            case CHASSIS_CHEST:
                speedMod = 0.0D;
                healthMod = 0.0D;
                // Should have more inventory space
                break;
            case CHASSIS_SOLID:
                speedMod = -0.05D;
                healthMod = 10.0D;
                break;
            default:
                break;
        }

        // Apply modifiers dynamically if changed (omitted for brevity, would need AttributeModifier)
    }

    // GeckoLib Implementation
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("walk", true));
        } else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("idle", true));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Nullable
    @Override
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable net.minecraft.entity.IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);

        // Randomize Parts
        this.dataManager.set(HEAD_TYPE, getRandomPart(ApparatiPartItem.PartType.HEAD_REDSTONE_ANTENNAE, ApparatiPartItem.PartType.HEAD_CAMERA_LENS, ApparatiPartItem.PartType.HEAD_MICROPHONE));
        this.dataManager.set(ARM_LEFT_TYPE, getRandomPart(ApparatiPartItem.PartType.ARM_HOLDER, ApparatiPartItem.PartType.ARM_PLACER));
        this.dataManager.set(ARM_RIGHT_TYPE, getRandomPart(ApparatiPartItem.PartType.ARM_HOLDER, ApparatiPartItem.PartType.ARM_PLACER));
        this.dataManager.set(CHASSIS_TYPE, getRandomPart(ApparatiPartItem.PartType.CHASSIS_HOLLOW, ApparatiPartItem.PartType.CHASSIS_CHEST, ApparatiPartItem.PartType.CHASSIS_SOLID));
        this.dataManager.set(TREADS_TYPE, ApparatiPartItem.PartType.TREADS_WHEELIE.ordinal());

        // Randomize Materials
        this.dataManager.set(HEAD_MATERIAL, getRandomMaterial());
        this.dataManager.set(ARM_LEFT_MATERIAL, getRandomMaterial());
        this.dataManager.set(ARM_RIGHT_MATERIAL, getRandomMaterial());
        this.dataManager.set(CHASSIS_MATERIAL, getRandomMaterial());
        this.dataManager.set(TREADS_MATERIAL, getRandomMaterial());

        return livingdata;
    }

    private int getRandomPart(ApparatiPartItem.PartType... types) {
        return types[this.rand.nextInt(types.length)].ordinal();
    }

    private String getRandomMaterial() {
        return VALID_MATERIALS[this.rand.nextInt(VALID_MATERIALS.length)];
    }

    // NBT Read/Write
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("HeadType", this.dataManager.get(HEAD_TYPE));
        compound.setInteger("ArmLeftType", this.dataManager.get(ARM_LEFT_TYPE));
        compound.setInteger("ArmRightType", this.dataManager.get(ARM_RIGHT_TYPE));
        compound.setInteger("ChassisType", this.dataManager.get(CHASSIS_TYPE));
        compound.setInteger("TreadsType", this.dataManager.get(TREADS_TYPE));

        compound.setString("HeadMaterial", this.dataManager.get(HEAD_MATERIAL));
        compound.setString("ArmLeftMaterial", this.dataManager.get(ARM_LEFT_MATERIAL));
        compound.setString("ArmRightMaterial", this.dataManager.get(ARM_RIGHT_MATERIAL));
        compound.setString("ChassisMaterial", this.dataManager.get(CHASSIS_MATERIAL));
        compound.setString("TreadsMaterial", this.dataManager.get(TREADS_MATERIAL));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(HEAD_TYPE, compound.getInteger("HeadType"));
        this.dataManager.set(ARM_LEFT_TYPE, compound.getInteger("ArmLeftType"));
        this.dataManager.set(ARM_RIGHT_TYPE, compound.getInteger("ArmRightType"));
        this.dataManager.set(CHASSIS_TYPE, compound.getInteger("ChassisType"));
        this.dataManager.set(TREADS_TYPE, compound.getInteger("TreadsType"));

        if (compound.hasKey("HeadMaterial")) this.dataManager.set(HEAD_MATERIAL, compound.getString("HeadMaterial"));
        if (compound.hasKey("ArmLeftMaterial")) this.dataManager.set(ARM_LEFT_MATERIAL, compound.getString("ArmLeftMaterial"));
        if (compound.hasKey("ArmRightMaterial")) this.dataManager.set(ARM_RIGHT_MATERIAL, compound.getString("ArmRightMaterial"));
        if (compound.hasKey("ChassisMaterial")) this.dataManager.set(CHASSIS_MATERIAL, compound.getString("ChassisMaterial"));
        if (compound.hasKey("TreadsMaterial")) this.dataManager.set(TREADS_MATERIAL, compound.getString("TreadsMaterial"));
    }
}
