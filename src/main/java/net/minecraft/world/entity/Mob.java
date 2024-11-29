package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.extensions.IForgeLivingEntity;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.fluids.FluidType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;

public abstract class Mob extends LivingEntity implements Targeting, IForgeLivingEntity {
   private static final EntityDataAccessor<Byte> DATA_MOB_FLAGS_ID = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BYTE);
   private static final int MOB_FLAG_NO_AI = 1;
   private static final int MOB_FLAG_LEFTHANDED = 2;
   private static final int MOB_FLAG_AGGRESSIVE = 4;
   protected static final int PICKUP_REACH = 1;
   private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 0, 1);
   public static final float MAX_WEARING_ARMOR_CHANCE = 0.15F;
   public static final float MAX_PICKUP_LOOT_CHANCE = 0.55F;
   public static final float MAX_ENCHANTED_ARMOR_CHANCE = 0.5F;
   public static final float MAX_ENCHANTED_WEAPON_CHANCE = 0.25F;
   public static final String LEASH_TAG = "Leash";
   public static final float DEFAULT_EQUIPMENT_DROP_CHANCE = 0.085F;
   public static final int PRESERVE_ITEM_DROP_CHANCE = 2;
   public static final int UPDATE_GOAL_SELECTOR_EVERY_N_TICKS = 2;
   public int ambientSoundTime;
   protected int xpReward;
   protected LookControl lookControl;
   protected MoveControl moveControl;
   protected JumpControl jumpControl;
   private final BodyRotationControl bodyRotationControl;
   protected PathNavigation navigation;
   protected final GoalSelector goalSelector;
   protected final GoalSelector targetSelector;
   @Nullable
   private LivingEntity target;
   private final Sensing sensing;
   private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
   protected final float[] handDropChances = new float[2];
   private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
   protected final float[] armorDropChances = new float[4];
   private boolean canPickUpLoot;
   private boolean persistenceRequired;
   private final Map<BlockPathTypes, Float> pathfindingMalus = Maps.newEnumMap(BlockPathTypes.class);
   @Nullable
   private ResourceLocation lootTable;
   private long lootTableSeed;
   @Nullable
   private Entity leashHolder;
   private int delayedLeashHolderId;
   @Nullable
   private CompoundTag leashInfoTag;
   private BlockPos restrictCenter = BlockPos.ZERO;
   private float restrictRadius = -1.0F;
   private MobSpawnType spawnType;
   private boolean spawnCancelled = false;

   protected Mob(EntityType<? extends Mob> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.goalSelector = new GoalSelector(pLevel.getProfilerSupplier());
      this.targetSelector = new GoalSelector(pLevel.getProfilerSupplier());
      this.lookControl = new LookControl(this);
      this.moveControl = new MoveControl(this);
      this.jumpControl = new JumpControl(this);
      this.bodyRotationControl = this.createBodyControl();
      this.navigation = this.createNavigation(pLevel);
      this.sensing = new Sensing(this);
      Arrays.fill(this.armorDropChances, 0.085F);
      Arrays.fill(this.handDropChances, 0.085F);
      if (pLevel != null && !pLevel.isClientSide) {
         this.registerGoals();
      }

   }

   protected void registerGoals() {
   }

   public static AttributeSupplier.Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0D).add(Attributes.ATTACK_KNOCKBACK);
   }

   protected PathNavigation createNavigation(Level pLevel) {
      return new GroundPathNavigation(this, pLevel);
   }

   protected boolean shouldPassengersInheritMalus() {
      return false;
   }

   public float getPathfindingMalus(BlockPathTypes pNodeType) {
      Mob mob;
      label17: {
         Entity entity = this.getControlledVehicle();
         if (entity instanceof Mob mob1) {
            if (mob1.shouldPassengersInheritMalus()) {
               mob = mob1;
               break label17;
            }
         }

         mob = this;
      }

      Float f = mob.pathfindingMalus.get(pNodeType);
      return f == null ? pNodeType.getMalus() : f;
   }

   public void setPathfindingMalus(BlockPathTypes pNodeType, float pMalus) {
      this.pathfindingMalus.put(pNodeType, pMalus);
   }

   public void onPathfindingStart() {
   }

   public void onPathfindingDone() {
   }

   protected BodyRotationControl createBodyControl() {
      return new BodyRotationControl(this);
   }

   public LookControl getLookControl() {
      return this.lookControl;
   }

   public MoveControl getMoveControl() {
      Entity entity = this.getControlledVehicle();
      if (entity instanceof Mob mob) {
         return mob.getMoveControl();
      } else {
         return this.moveControl;
      }
   }

   public JumpControl getJumpControl() {
      return this.jumpControl;
   }

   public PathNavigation getNavigation() {
      Entity entity = this.getControlledVehicle();
      if (entity instanceof Mob mob) {
         return mob.getNavigation();
      } else {
         return this.navigation;
      }
   }

   @Nullable
   public LivingEntity getControllingPassenger() {
      if (!this.isNoAi()) {
         Entity entity = this.getFirstPassenger();
         if (entity instanceof Mob) {
            return (Mob)entity;
         }
      }

      return null;
   }

   public Sensing getSensing() {
      return this.sensing;
   }

   @Nullable
   public LivingEntity getTarget() {
      return this.target;
   }

   public void setTarget(@Nullable LivingEntity pTarget) {
      if (Reflector.ForgeHooks_onLivingChangeTarget.exists()) {
         LivingChangeTargetEvent livingchangetargetevent = (LivingChangeTargetEvent)Reflector.ForgeHooks_onLivingChangeTarget.call(this, pTarget, LivingChangeTargetEvent.LivingTargetType.MOB_TARGET);
         if (!livingchangetargetevent.isCanceled()) {
            this.target = livingchangetargetevent.getNewTarget();
         }

      } else {
         this.target = pTarget;
      }
   }

   public boolean canAttackType(EntityType<?> pType) {
      return pType != EntityType.GHAST;
   }

   public boolean canFireProjectileWeapon(ProjectileWeaponItem pProjectileWeapon) {
      return false;
   }

   public void ate() {
      this.gameEvent(GameEvent.EAT);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_MOB_FLAGS_ID, (byte)0);
   }

   public int getAmbientSoundInterval() {
      return 80;
   }

   public void playAmbientSound() {
      SoundEvent soundevent = this.getAmbientSound();
      if (soundevent != null) {
         this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
      }

   }

   public void baseTick() {
      super.baseTick();
      this.level().getProfiler().push("mobBaseTick");
      if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundTime++) {
         this.resetAmbientSoundTime();
         this.playAmbientSound();
      }

      this.level().getProfiler().pop();
   }

   protected void playHurtSound(DamageSource pSource) {
      this.resetAmbientSoundTime();
      super.playHurtSound(pSource);
   }

   private void resetAmbientSoundTime() {
      this.ambientSoundTime = -this.getAmbientSoundInterval();
   }

   public int getExperienceReward() {
      if (this.xpReward > 0) {
         int i = this.xpReward;

         for(int j = 0; j < this.armorItems.size(); ++j) {
            if (!this.armorItems.get(j).isEmpty() && this.armorDropChances[j] <= 1.0F) {
               i += 1 + this.random.nextInt(3);
            }
         }

         for(int k = 0; k < this.handItems.size(); ++k) {
            if (!this.handItems.get(k).isEmpty() && this.handDropChances[k] <= 1.0F) {
               i += 1 + this.random.nextInt(3);
            }
         }

         return i;
      } else {
         return this.xpReward;
      }
   }

   public void spawnAnim() {
      if (this.level().isClientSide) {
         for(int i = 0; i < 20; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            double d3 = 10.0D;
            this.level().addParticle(ParticleTypes.POOF, this.getX(1.0D) - d0 * 10.0D, this.getRandomY() - d1 * 10.0D, this.getRandomZ(1.0D) - d2 * 10.0D, d0, d1, d2);
         }
      } else {
         this.level().broadcastEntityEvent(this, (byte)20);
      }

   }

   public void handleEntityEvent(byte pId) {
      if (pId == 20) {
         this.spawnAnim();
      } else {
         super.handleEntityEvent(pId);
      }

   }

   public void tick() {
      if (Config.isSmoothWorld() && this.canSkipUpdate()) {
         this.onUpdateMinimal();
      } else {
         super.tick();
         if (!this.level().isClientSide) {
            this.tickLeash();
            if (this.tickCount % 5 == 0) {
               this.updateControlFlags();
            }
         }

      }
   }

   protected void updateControlFlags() {
      boolean flag = !(this.getControllingPassenger() instanceof Mob);
      boolean flag1 = !(this.getVehicle() instanceof Boat);
      this.goalSelector.setControlFlag(Goal.Flag.MOVE, flag);
      this.goalSelector.setControlFlag(Goal.Flag.JUMP, flag && flag1);
      this.goalSelector.setControlFlag(Goal.Flag.LOOK, flag);
   }

   protected float tickHeadTurn(float pYRot, float pAnimStep) {
      this.bodyRotationControl.clientTick();
      return pAnimStep;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      return null;
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("CanPickUpLoot", this.canPickUpLoot());
      pCompound.putBoolean("PersistenceRequired", this.persistenceRequired);
      ListTag listtag = new ListTag();

      for(ItemStack itemstack : this.armorItems) {
         CompoundTag compoundtag = new CompoundTag();
         if (!itemstack.isEmpty()) {
            itemstack.save(compoundtag);
         }

         listtag.add(compoundtag);
      }

      pCompound.put("ArmorItems", listtag);
      ListTag listtag1 = new ListTag();

      for(ItemStack itemstack1 : this.handItems) {
         CompoundTag compoundtag1 = new CompoundTag();
         if (!itemstack1.isEmpty()) {
            itemstack1.save(compoundtag1);
         }

         listtag1.add(compoundtag1);
      }

      pCompound.put("HandItems", listtag1);
      ListTag listtag2 = new ListTag();

      for(float f : this.armorDropChances) {
         listtag2.add(FloatTag.valueOf(f));
      }

      pCompound.put("ArmorDropChances", listtag2);
      ListTag listtag3 = new ListTag();

      for(float f1 : this.handDropChances) {
         listtag3.add(FloatTag.valueOf(f1));
      }

      pCompound.put("HandDropChances", listtag3);
      if (this.leashHolder != null) {
         CompoundTag compoundtag2 = new CompoundTag();
         if (this.leashHolder instanceof LivingEntity) {
            UUID uuid = this.leashHolder.getUUID();
            compoundtag2.putUUID("UUID", uuid);
         } else if (this.leashHolder instanceof HangingEntity) {
            BlockPos blockpos = ((HangingEntity)this.leashHolder).getPos();
            compoundtag2.putInt("X", blockpos.getX());
            compoundtag2.putInt("Y", blockpos.getY());
            compoundtag2.putInt("Z", blockpos.getZ());
         }

         pCompound.put("Leash", compoundtag2);
      } else if (this.leashInfoTag != null) {
         pCompound.put("Leash", this.leashInfoTag.copy());
      }

      pCompound.putBoolean("LeftHanded", this.isLeftHanded());
      if (this.lootTable != null) {
         pCompound.putString("DeathLootTable", this.lootTable.toString());
         if (this.lootTableSeed != 0L) {
            pCompound.putLong("DeathLootTableSeed", this.lootTableSeed);
         }
      }

      if (this.isNoAi()) {
         pCompound.putBoolean("NoAI", this.isNoAi());
      }

      if (this.spawnType != null) {
         pCompound.putString("forge:spawn_type", this.spawnType.name());
      }

   }

   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("CanPickUpLoot", 1)) {
         this.setCanPickUpLoot(pCompound.getBoolean("CanPickUpLoot"));
      }

      this.persistenceRequired = pCompound.getBoolean("PersistenceRequired");
      if (pCompound.contains("ArmorItems", 9)) {
         ListTag listtag = pCompound.getList("ArmorItems", 10);

         for(int i = 0; i < this.armorItems.size(); ++i) {
            this.armorItems.set(i, ItemStack.of(listtag.getCompound(i)));
         }
      }

      if (pCompound.contains("HandItems", 9)) {
         ListTag listtag1 = pCompound.getList("HandItems", 10);

         for(int j = 0; j < this.handItems.size(); ++j) {
            this.handItems.set(j, ItemStack.of(listtag1.getCompound(j)));
         }
      }

      if (pCompound.contains("ArmorDropChances", 9)) {
         ListTag listtag2 = pCompound.getList("ArmorDropChances", 5);

         for(int k = 0; k < listtag2.size(); ++k) {
            this.armorDropChances[k] = listtag2.getFloat(k);
         }
      }

      if (pCompound.contains("HandDropChances", 9)) {
         ListTag listtag3 = pCompound.getList("HandDropChances", 5);

         for(int l = 0; l < listtag3.size(); ++l) {
            this.handDropChances[l] = listtag3.getFloat(l);
         }
      }

      if (pCompound.contains("Leash", 10)) {
         this.leashInfoTag = pCompound.getCompound("Leash");
      }

      this.setLeftHanded(pCompound.getBoolean("LeftHanded"));
      if (pCompound.contains("DeathLootTable", 8)) {
         this.lootTable = new ResourceLocation(pCompound.getString("DeathLootTable"));
         this.lootTableSeed = pCompound.getLong("DeathLootTableSeed");
      }

      this.setNoAi(pCompound.getBoolean("NoAI"));
      if (pCompound.contains("forge:spawn_type")) {
         try {
            this.spawnType = MobSpawnType.valueOf(pCompound.getString("forge:spawn_type"));
         } catch (Exception exception) {
            pCompound.remove("forge:spawn_type");
         }
      }

   }

   protected void dropFromLootTable(DamageSource pDamageSource, boolean pAttackedRecently) {
      super.dropFromLootTable(pDamageSource, pAttackedRecently);
      this.lootTable = null;
   }

   public final ResourceLocation getLootTable() {
      return this.lootTable == null ? this.getDefaultLootTable() : this.lootTable;
   }

   protected ResourceLocation getDefaultLootTable() {
      return super.getLootTable();
   }

   public long getLootTableSeed() {
      return this.lootTableSeed;
   }

   public void setZza(float pAmount) {
      this.zza = pAmount;
   }

   public void setYya(float pAmount) {
      this.yya = pAmount;
   }

   public void setXxa(float pAmount) {
      this.xxa = pAmount;
   }

   public void setSpeed(float pSpeed) {
      super.setSpeed(pSpeed);
      this.setZza(pSpeed);
   }

   public void aiStep() {
      super.aiStep();
      this.level().getProfiler().push("looting");
      boolean flag = this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
      if (Reflector.ForgeEventFactory_getMobGriefingEvent.exists()) {
         flag = Reflector.callBoolean(Reflector.ForgeEventFactory_getMobGriefingEvent, this.level(), this);
      }

      if (!this.level().isClientSide && this.canPickUpLoot() && this.isAlive() && !this.dead && flag) {
         Vec3i vec3i = this.getPickupReach();

         for(ItemEntity itementity : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate((double)vec3i.getX(), (double)vec3i.getY(), (double)vec3i.getZ()))) {
            if (!itementity.isRemoved() && !itementity.getItem().isEmpty() && !itementity.hasPickUpDelay() && this.wantsToPickUp(itementity.getItem())) {
               this.pickUpItem(itementity);
            }
         }
      }

      this.level().getProfiler().pop();
   }

   protected Vec3i getPickupReach() {
      return ITEM_PICKUP_REACH;
   }

   protected void pickUpItem(ItemEntity pItemEntity) {
      ItemStack itemstack = pItemEntity.getItem();
      ItemStack itemstack1 = this.equipItemIfPossible(itemstack.copy());
      if (!itemstack1.isEmpty()) {
         this.onItemPickup(pItemEntity);
         this.take(pItemEntity, itemstack1.getCount());
         itemstack.shrink(itemstack1.getCount());
         if (itemstack.isEmpty()) {
            pItemEntity.discard();
         }
      }

   }

   public ItemStack equipItemIfPossible(ItemStack pStack) {
      EquipmentSlot equipmentslot = getEquipmentSlotForItem(pStack);
      ItemStack itemstack = this.getItemBySlot(equipmentslot);
      boolean flag = this.canReplaceCurrentItem(pStack, itemstack);
      if (equipmentslot.isArmor() && !flag) {
         equipmentslot = EquipmentSlot.MAINHAND;
         itemstack = this.getItemBySlot(equipmentslot);
         flag = itemstack.isEmpty();
      }

      if (flag && this.canHoldItem(pStack)) {
         double d0 = (double)this.getEquipmentDropChance(equipmentslot);
         if (!itemstack.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1F, 0.0F) < d0) {
            this.spawnAtLocation(itemstack);
         }

         if (equipmentslot.isArmor() && pStack.getCount() > 1) {
            ItemStack itemstack1 = pStack.copyWithCount(1);
            this.setItemSlotAndDropWhenKilled(equipmentslot, itemstack1);
            return itemstack1;
         } else {
            this.setItemSlotAndDropWhenKilled(equipmentslot, pStack);
            return pStack;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   protected void setItemSlotAndDropWhenKilled(EquipmentSlot pSlot, ItemStack pStack) {
      this.setItemSlot(pSlot, pStack);
      this.setGuaranteedDrop(pSlot);
      this.persistenceRequired = true;
   }

   public void setGuaranteedDrop(EquipmentSlot pSlot) {
      switch (pSlot.getType()) {
         case HAND:
            this.handDropChances[pSlot.getIndex()] = 2.0F;
            break;
         case ARMOR:
            this.armorDropChances[pSlot.getIndex()] = 2.0F;
      }

   }

   protected boolean canReplaceCurrentItem(ItemStack pCandidate, ItemStack pExisting) {
      if (pExisting.isEmpty()) {
         return true;
      } else if (pCandidate.getItem() instanceof SwordItem) {
         if (!(pExisting.getItem() instanceof SwordItem)) {
            return true;
         } else {
            SwordItem sworditem = (SwordItem)pCandidate.getItem();
            SwordItem sworditem1 = (SwordItem)pExisting.getItem();
            if (sworditem.getDamage() != sworditem1.getDamage()) {
               return sworditem.getDamage() > sworditem1.getDamage();
            } else {
               return this.canReplaceEqualItem(pCandidate, pExisting);
            }
         }
      } else if (pCandidate.getItem() instanceof BowItem && pExisting.getItem() instanceof BowItem) {
         return this.canReplaceEqualItem(pCandidate, pExisting);
      } else if (pCandidate.getItem() instanceof CrossbowItem && pExisting.getItem() instanceof CrossbowItem) {
         return this.canReplaceEqualItem(pCandidate, pExisting);
      } else if (pCandidate.getItem() instanceof ArmorItem) {
         if (EnchantmentHelper.hasBindingCurse(pExisting)) {
            return false;
         } else if (!(pExisting.getItem() instanceof ArmorItem)) {
            return true;
         } else {
            ArmorItem armoritem = (ArmorItem)pCandidate.getItem();
            ArmorItem armoritem1 = (ArmorItem)pExisting.getItem();
            if (armoritem.getDefense() != armoritem1.getDefense()) {
               return armoritem.getDefense() > armoritem1.getDefense();
            } else if (armoritem.getToughness() != armoritem1.getToughness()) {
               return armoritem.getToughness() > armoritem1.getToughness();
            } else {
               return this.canReplaceEqualItem(pCandidate, pExisting);
            }
         }
      } else {
         if (pCandidate.getItem() instanceof DiggerItem) {
            if (pExisting.getItem() instanceof BlockItem) {
               return true;
            }

            if (pExisting.getItem() instanceof DiggerItem) {
               DiggerItem diggeritem = (DiggerItem)pCandidate.getItem();
               DiggerItem diggeritem1 = (DiggerItem)pExisting.getItem();
               if (diggeritem.getAttackDamage() != diggeritem1.getAttackDamage()) {
                  return diggeritem.getAttackDamage() > diggeritem1.getAttackDamage();
               }

               return this.canReplaceEqualItem(pCandidate, pExisting);
            }
         }

         return false;
      }
   }

   public boolean canReplaceEqualItem(ItemStack pCandidate, ItemStack pExisting) {
      if (pCandidate.getDamageValue() >= pExisting.getDamageValue() && (!pCandidate.hasTag() || pExisting.hasTag())) {
         if (pCandidate.hasTag() && pExisting.hasTag()) {
            return pCandidate.getTag().getAllKeys().stream().anyMatch((p_21512_0_) -> {
               return !p_21512_0_.equals("Damage");
            }) && !pExisting.getTag().getAllKeys().stream().anyMatch((p_21502_0_) -> {
               return !p_21502_0_.equals("Damage");
            });
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public boolean canHoldItem(ItemStack pStack) {
      return true;
   }

   public boolean wantsToPickUp(ItemStack pStack) {
      return this.canHoldItem(pStack);
   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return true;
   }

   public boolean requiresCustomPersistence() {
      return this.isPassenger();
   }

   protected boolean shouldDespawnInPeaceful() {
      return false;
   }

   public void checkDespawn() {
      if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
         this.discard();
      } else if (!this.isPersistenceRequired() && !this.requiresCustomPersistence()) {
         Entity entity = this.level().getNearestPlayer(this, -1.0D);
         if (Reflector.ForgeEventFactory_canEntityDespawn.exists()) {
            Object object = Reflector.ForgeEventFactory_canEntityDespawn.call(this, this.level());
            if (object == ReflectorForge.EVENT_RESULT_DENY) {
               this.noActionTime = 0;
               entity = null;
            } else if (object == ReflectorForge.EVENT_RESULT_ALLOW) {
               this.discard();
               entity = null;
            }
         }

         if (entity != null) {
            double d0 = entity.distanceToSqr(this);
            int i = this.getType().getCategory().getDespawnDistance();
            int j = i * i;
            if (d0 > (double)j && this.removeWhenFarAway(d0)) {
               this.discard();
            }

            int k = this.getType().getCategory().getNoDespawnDistance();
            int l = k * k;
            if (this.noActionTime > 600 && this.random.nextInt(800) == 0 && d0 > (double)l && this.removeWhenFarAway(d0)) {
               this.discard();
            } else if (d0 < (double)l) {
               this.noActionTime = 0;
            }
         }
      } else {
         this.noActionTime = 0;
      }

   }

   protected final void serverAiStep() {
      ++this.noActionTime;
      this.level().getProfiler().push("sensing");
      this.sensing.tick();
      this.level().getProfiler().pop();
      int i = this.level().getServer().getTickCount() + this.getId();
      if (i % 2 != 0 && this.tickCount > 1) {
         this.level().getProfiler().push("targetSelector");
         this.targetSelector.tickRunningGoals(false);
         this.level().getProfiler().pop();
         this.level().getProfiler().push("goalSelector");
         this.goalSelector.tickRunningGoals(false);
         this.level().getProfiler().pop();
      } else {
         this.level().getProfiler().push("targetSelector");
         this.targetSelector.tick();
         this.level().getProfiler().pop();
         this.level().getProfiler().push("goalSelector");
         this.goalSelector.tick();
         this.level().getProfiler().pop();
      }

      this.level().getProfiler().push("navigation");
      this.navigation.tick();
      this.level().getProfiler().pop();
      this.level().getProfiler().push("mob tick");
      this.customServerAiStep();
      this.level().getProfiler().pop();
      this.level().getProfiler().push("controls");
      this.level().getProfiler().push("move");
      this.moveControl.tick();
      this.level().getProfiler().popPush("look");
      this.lookControl.tick();
      this.level().getProfiler().popPush("jump");
      this.jumpControl.tick();
      this.level().getProfiler().pop();
      this.level().getProfiler().pop();
      this.sendDebugPackets();
   }

   protected void sendDebugPackets() {
      DebugPackets.sendGoalSelector(this.level(), this, this.goalSelector);
   }

   protected void customServerAiStep() {
   }

   public int getMaxHeadXRot() {
      return 40;
   }

   public int getMaxHeadYRot() {
      return 75;
   }

   public int getHeadRotSpeed() {
      return 10;
   }

   public void lookAt(Entity pEntity, float pMaxYRotIncrease, float pMaxXRotIncrease) {
      double d0 = pEntity.getX() - this.getX();
      double d1 = pEntity.getZ() - this.getZ();
      double d2;
      if (pEntity instanceof LivingEntity livingentity) {
         d2 = livingentity.getEyeY() - this.getEyeY();
      } else {
         d2 = (pEntity.getBoundingBox().minY + pEntity.getBoundingBox().maxY) / 2.0D - this.getEyeY();
      }

      double d3 = Math.sqrt(d0 * d0 + d1 * d1);
      float f = (float)(Mth.atan2(d1, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
      float f1 = (float)(-(Mth.atan2(d2, d3) * (double)(180F / (float)Math.PI)));
      this.setXRot(this.rotlerp(this.getXRot(), f1, pMaxXRotIncrease));
      this.setYRot(this.rotlerp(this.getYRot(), f, pMaxYRotIncrease));
   }

   private float rotlerp(float pAngle, float pTargetAngle, float pMaxIncrease) {
      float f = Mth.wrapDegrees(pTargetAngle - pAngle);
      if (f > pMaxIncrease) {
         f = pMaxIncrease;
      }

      if (f < -pMaxIncrease) {
         f = -pMaxIncrease;
      }

      return pAngle + f;
   }

   public static boolean checkMobSpawnRules(EntityType<? extends Mob> pType, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      BlockPos blockpos = pPos.below();
      return pSpawnType == MobSpawnType.SPAWNER || pLevel.getBlockState(blockpos).isValidSpawn(pLevel, blockpos, pType);
   }

   public boolean checkSpawnRules(LevelAccessor pLevel, MobSpawnType pReason) {
      return true;
   }

   public boolean checkSpawnObstruction(LevelReader pLevel) {
      return !pLevel.containsAnyLiquid(this.getBoundingBox()) && pLevel.isUnobstructed(this);
   }

   public int getMaxSpawnClusterSize() {
      return 4;
   }

   public boolean isMaxGroupSizeReached(int pSize) {
      return false;
   }

   public int getMaxFallDistance() {
      if (this.getTarget() == null) {
         return 3;
      } else {
         int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33F);
         i -= (3 - this.level().getDifficulty().getId()) * 4;
         if (i < 0) {
            i = 0;
         }

         return i + 3;
      }
   }

   public Iterable<ItemStack> getHandSlots() {
      return this.handItems;
   }

   public Iterable<ItemStack> getArmorSlots() {
      return this.armorItems;
   }

   public ItemStack getItemBySlot(EquipmentSlot pSlot) {
      switch (pSlot.getType()) {
         case HAND:
            return this.handItems.get(pSlot.getIndex());
         case ARMOR:
            return this.armorItems.get(pSlot.getIndex());
         default:
            return ItemStack.EMPTY;
      }
   }

   public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {
      this.verifyEquippedItem(pStack);
      switch (pSlot.getType()) {
         case HAND:
            this.onEquipItem(pSlot, this.handItems.set(pSlot.getIndex(), pStack), pStack);
            break;
         case ARMOR:
            this.onEquipItem(pSlot, this.armorItems.set(pSlot.getIndex(), pStack), pStack);
      }

   }

   protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
      super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);

      for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
         ItemStack itemstack = this.getItemBySlot(equipmentslot);
         float f = this.getEquipmentDropChance(equipmentslot);
         boolean flag = f > 1.0F;
         if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack) && (pRecentlyHit || flag) && Math.max(this.random.nextFloat() - (float)pLooting * 0.01F, 0.0F) < f) {
            if (!flag && itemstack.isDamageableItem()) {
               itemstack.setDamageValue(itemstack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemstack.getMaxDamage() - 3, 1))));
            }

            this.spawnAtLocation(itemstack);
            this.setItemSlot(equipmentslot, ItemStack.EMPTY);
         }
      }

   }

   protected float getEquipmentDropChance(EquipmentSlot pSlot) {
      float f;
      switch (pSlot.getType()) {
         case HAND:
            f = this.handDropChances[pSlot.getIndex()];
            break;
         case ARMOR:
            f = this.armorDropChances[pSlot.getIndex()];
            break;
         default:
            f = 0.0F;
      }

      return f;
   }

   protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
      if (pRandom.nextFloat() < 0.15F * pDifficulty.getSpecialMultiplier()) {
         int i = pRandom.nextInt(2);
         float f = this.level().getDifficulty() == Difficulty.HARD ? 0.1F : 0.25F;
         if (pRandom.nextFloat() < 0.095F) {
            ++i;
         }

         if (pRandom.nextFloat() < 0.095F) {
            ++i;
         }

         if (pRandom.nextFloat() < 0.095F) {
            ++i;
         }

         boolean flag = true;

         for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR) {
               ItemStack itemstack = this.getItemBySlot(equipmentslot);
               if (!flag && pRandom.nextFloat() < f) {
                  break;
               }

               flag = false;
               if (itemstack.isEmpty()) {
                  Item item = getEquipmentForSlot(equipmentslot, i);
                  if (item != null) {
                     this.setItemSlot(equipmentslot, new ItemStack(item));
                  }
               }
            }
         }
      }

   }

   @Nullable
   public static Item getEquipmentForSlot(EquipmentSlot pSlot, int pChance) {
      switch (pSlot) {
         case HEAD:
            if (pChance == 0) {
               return Items.LEATHER_HELMET;
            } else if (pChance == 1) {
               return Items.GOLDEN_HELMET;
            } else if (pChance == 2) {
               return Items.CHAINMAIL_HELMET;
            } else if (pChance == 3) {
               return Items.IRON_HELMET;
            } else if (pChance == 4) {
               return Items.DIAMOND_HELMET;
            }
         case CHEST:
            if (pChance == 0) {
               return Items.LEATHER_CHESTPLATE;
            } else if (pChance == 1) {
               return Items.GOLDEN_CHESTPLATE;
            } else if (pChance == 2) {
               return Items.CHAINMAIL_CHESTPLATE;
            } else if (pChance == 3) {
               return Items.IRON_CHESTPLATE;
            } else if (pChance == 4) {
               return Items.DIAMOND_CHESTPLATE;
            }
         case LEGS:
            if (pChance == 0) {
               return Items.LEATHER_LEGGINGS;
            } else if (pChance == 1) {
               return Items.GOLDEN_LEGGINGS;
            } else if (pChance == 2) {
               return Items.CHAINMAIL_LEGGINGS;
            } else if (pChance == 3) {
               return Items.IRON_LEGGINGS;
            } else if (pChance == 4) {
               return Items.DIAMOND_LEGGINGS;
            }
         case FEET:
            if (pChance == 0) {
               return Items.LEATHER_BOOTS;
            } else if (pChance == 1) {
               return Items.GOLDEN_BOOTS;
            } else if (pChance == 2) {
               return Items.CHAINMAIL_BOOTS;
            } else if (pChance == 3) {
               return Items.IRON_BOOTS;
            } else if (pChance == 4) {
               return Items.DIAMOND_BOOTS;
            }
         default:
            return null;
      }
   }

   protected void populateDefaultEquipmentEnchantments(RandomSource pRandom, DifficultyInstance pDifficulty) {
      float f = pDifficulty.getSpecialMultiplier();
      this.enchantSpawnedWeapon(pRandom, f);

      for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
         if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR) {
            this.enchantSpawnedArmor(pRandom, f, equipmentslot);
         }
      }

   }

   protected void enchantSpawnedWeapon(RandomSource pRandom, float pChanceMultiplier) {
      if (!this.getMainHandItem().isEmpty() && pRandom.nextFloat() < 0.25F * pChanceMultiplier) {
         this.setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(pRandom, this.getMainHandItem(), (int)(5.0F + pChanceMultiplier * (float)pRandom.nextInt(18)), false));
      }

   }

   protected void enchantSpawnedArmor(RandomSource pRandom, float pChanceMultiplier, EquipmentSlot pSlot) {
      ItemStack itemstack = this.getItemBySlot(pSlot);
      if (!itemstack.isEmpty() && pRandom.nextFloat() < 0.5F * pChanceMultiplier) {
         this.setItemSlot(pSlot, EnchantmentHelper.enchantItem(pRandom, itemstack, (int)(5.0F + pChanceMultiplier * (float)pRandom.nextInt(18)), false));
      }

   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      RandomSource randomsource = pLevel.getRandom();
      this.getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier("Random spawn bonus", randomsource.triangle(0.0D, 0.11485000000000001D), AttributeModifier.Operation.MULTIPLY_BASE));
      if (randomsource.nextFloat() < 0.05F) {
         this.setLeftHanded(true);
      } else {
         this.setLeftHanded(false);
      }

      this.spawnType = pReason;
      return pSpawnData;
   }

   public void setPersistenceRequired() {
      this.persistenceRequired = true;
   }

   public void setDropChance(EquipmentSlot pSlot, float pChance) {
      switch (pSlot.getType()) {
         case HAND:
            this.handDropChances[pSlot.getIndex()] = pChance;
            break;
         case ARMOR:
            this.armorDropChances[pSlot.getIndex()] = pChance;
      }

   }

   public boolean canPickUpLoot() {
      return this.canPickUpLoot;
   }

   public void setCanPickUpLoot(boolean pCanPickUpLoot) {
      this.canPickUpLoot = pCanPickUpLoot;
   }

   public boolean canTakeItem(ItemStack pItemstack) {
      EquipmentSlot equipmentslot = getEquipmentSlotForItem(pItemstack);
      return this.getItemBySlot(equipmentslot).isEmpty() && this.canPickUpLoot();
   }

   public boolean isPersistenceRequired() {
      return this.persistenceRequired;
   }

   public final InteractionResult interact(Player pPlayer, InteractionHand pHand) {
      if (!this.isAlive()) {
         return InteractionResult.PASS;
      } else if (this.getLeashHolder() == pPlayer) {
         this.dropLeash(true, !pPlayer.getAbilities().instabuild);
         this.gameEvent(GameEvent.ENTITY_INTERACT, pPlayer);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         InteractionResult interactionresult = this.checkAndHandleImportantInteractions(pPlayer, pHand);
         if (interactionresult.consumesAction()) {
            this.gameEvent(GameEvent.ENTITY_INTERACT, pPlayer);
            return interactionresult;
         } else {
            interactionresult = this.mobInteract(pPlayer, pHand);
            if (interactionresult.consumesAction()) {
               this.gameEvent(GameEvent.ENTITY_INTERACT, pPlayer);
               return interactionresult;
            } else {
               return super.interact(pPlayer, pHand);
            }
         }
      }
   }

   private InteractionResult checkAndHandleImportantInteractions(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.is(Items.LEAD) && this.canBeLeashed(pPlayer)) {
         this.setLeashedTo(pPlayer, true);
         itemstack.shrink(1);
         return InteractionResult.sidedSuccess(this.level().isClientSide);
      } else {
         if (itemstack.is(Items.NAME_TAG)) {
            InteractionResult interactionresult = itemstack.interactLivingEntity(pPlayer, this, pHand);
            if (interactionresult.consumesAction()) {
               return interactionresult;
            }
         }

         if (itemstack.getItem() instanceof SpawnEggItem) {
            if (this.level() instanceof ServerLevel) {
               SpawnEggItem spawneggitem = (SpawnEggItem)itemstack.getItem();
               Optional<Mob> optional = spawneggitem.spawnOffspringFromSpawnEgg(pPlayer, this, (EntityType<? extends Mob>) this.getType(), (ServerLevel)this.level(), this.position(), itemstack);
               optional.ifPresent((p_21474_2_) -> {
                  this.onOffspringSpawnedFromEgg(pPlayer, p_21474_2_);
               });
               return optional.isPresent() ? InteractionResult.SUCCESS : InteractionResult.PASS;
            } else {
               return InteractionResult.CONSUME;
            }
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   protected void onOffspringSpawnedFromEgg(Player pPlayer, Mob pChild) {
   }

   protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      return InteractionResult.PASS;
   }

   public boolean isWithinRestriction() {
      return this.isWithinRestriction(this.blockPosition());
   }

   public boolean isWithinRestriction(BlockPos pPos) {
      if (this.restrictRadius == -1.0F) {
         return true;
      } else {
         return this.restrictCenter.distSqr(pPos) < (double)(this.restrictRadius * this.restrictRadius);
      }
   }

   public void restrictTo(BlockPos pPos, int pDistance) {
      this.restrictCenter = pPos;
      this.restrictRadius = (float)pDistance;
   }

   public BlockPos getRestrictCenter() {
      return this.restrictCenter;
   }

   public float getRestrictRadius() {
      return this.restrictRadius;
   }

   public void clearRestriction() {
      this.restrictRadius = -1.0F;
   }

   public boolean hasRestriction() {
      return this.restrictRadius != -1.0F;
   }

   @Nullable
   public <T extends Mob> T convertTo(EntityType<T> pEntityType, boolean pTransferInventory) {
      if (this.isRemoved()) {
         return (T)(null);
      } else {
         T t = pEntityType.create(this.level());
         if (t == null) {
            return (T)(null);
         } else {
            t.copyPosition(this);
            t.setBaby(this.isBaby());
            t.setNoAi(this.isNoAi());
            if (this.hasCustomName()) {
               t.setCustomName(this.getCustomName());
               t.setCustomNameVisible(this.isCustomNameVisible());
            }

            if (this.isPersistenceRequired()) {
               t.setPersistenceRequired();
            }

            t.setInvulnerable(this.isInvulnerable());
            if (pTransferInventory) {
               t.setCanPickUpLoot(this.canPickUpLoot());

               for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
                  ItemStack itemstack = this.getItemBySlot(equipmentslot);
                  if (!itemstack.isEmpty()) {
                     t.setItemSlot(equipmentslot, itemstack.copyAndClear());
                     t.setDropChance(equipmentslot, this.getEquipmentDropChance(equipmentslot));
                  }
               }
            }

            this.level().addFreshEntity(t);
            if (this.isPassenger()) {
               Entity entity = this.getVehicle();
               this.stopRiding();
               t.startRiding(entity, true);
            }

            this.discard();
            return t;
         }
      }
   }

   protected void tickLeash() {
      if (this.leashInfoTag != null) {
         this.restoreLeashFromSave();
      }

      if (this.leashHolder != null && (!this.isAlive() || !this.leashHolder.isAlive())) {
         this.dropLeash(true, true);
      }

   }

   public void dropLeash(boolean pBroadcastPacket, boolean pDropLeash) {
      if (this.leashHolder != null) {
         this.leashHolder = null;
         this.leashInfoTag = null;
         if (!this.level().isClientSide && pDropLeash) {
            this.spawnAtLocation(Items.LEAD);
         }

         if (!this.level().isClientSide && pBroadcastPacket && this.level() instanceof ServerLevel) {
            ((ServerLevel)this.level()).getChunkSource().broadcast(this, new ClientboundSetEntityLinkPacket(this, (Entity)null));
         }
      }

   }

   public boolean canBeLeashed(Player pPlayer) {
      return !this.isLeashed() && !(this instanceof Enemy);
   }

   public boolean isLeashed() {
      return this.leashHolder != null;
   }

   @Nullable
   public Entity getLeashHolder() {
      if (this.leashHolder == null && this.delayedLeashHolderId != 0 && this.level().isClientSide) {
         this.leashHolder = this.level().getEntity(this.delayedLeashHolderId);
      }

      return this.leashHolder;
   }

   public void setLeashedTo(Entity pLeashHolder, boolean pBroadcastPacket) {
      this.leashHolder = pLeashHolder;
      this.leashInfoTag = null;
      if (!this.level().isClientSide && pBroadcastPacket && this.level() instanceof ServerLevel) {
         ((ServerLevel)this.level()).getChunkSource().broadcast(this, new ClientboundSetEntityLinkPacket(this, this.leashHolder));
      }

      if (this.isPassenger()) {
         this.stopRiding();
      }

   }

   public void setDelayedLeashHolderId(int pLeashHolderID) {
      this.delayedLeashHolderId = pLeashHolderID;
      this.dropLeash(false, false);
   }

   public boolean startRiding(Entity pEntity, boolean pForce) {
      boolean flag = super.startRiding(pEntity, pForce);
      if (flag && this.isLeashed()) {
         this.dropLeash(true, true);
      }

      return flag;
   }

   private void restoreLeashFromSave() {
      if (this.leashInfoTag != null && this.level() instanceof ServerLevel) {
         if (this.leashInfoTag.hasUUID("UUID")) {
            UUID uuid = this.leashInfoTag.getUUID("UUID");
            Entity entity = ((ServerLevel)this.level()).getEntity(uuid);
            if (entity != null) {
               this.setLeashedTo(entity, true);
               return;
            }
         } else if (this.leashInfoTag.contains("X", 99) && this.leashInfoTag.contains("Y", 99) && this.leashInfoTag.contains("Z", 99)) {
            BlockPos blockpos = NbtUtils.readBlockPos(this.leashInfoTag);
            this.setLeashedTo(LeashFenceKnotEntity.getOrCreateKnot(this.level(), blockpos), true);
            return;
         }

         if (this.tickCount > 100) {
            this.spawnAtLocation(Items.LEAD);
            this.leashInfoTag = null;
         }
      }

   }

   public boolean isEffectiveAi() {
      return super.isEffectiveAi() && !this.isNoAi();
   }

   public void setNoAi(boolean pNoAi) {
      byte b0 = this.entityData.get(DATA_MOB_FLAGS_ID);
      this.entityData.set(DATA_MOB_FLAGS_ID, pNoAi ? (byte)(b0 | 1) : (byte)(b0 & -2));
   }

   public void setLeftHanded(boolean pLeftHanded) {
      byte b0 = this.entityData.get(DATA_MOB_FLAGS_ID);
      this.entityData.set(DATA_MOB_FLAGS_ID, pLeftHanded ? (byte)(b0 | 2) : (byte)(b0 & -3));
   }

   public void setAggressive(boolean pAggressive) {
      byte b0 = this.entityData.get(DATA_MOB_FLAGS_ID);
      this.entityData.set(DATA_MOB_FLAGS_ID, pAggressive ? (byte)(b0 | 4) : (byte)(b0 & -5));
   }

   public boolean isNoAi() {
      return (this.entityData.get(DATA_MOB_FLAGS_ID) & 1) != 0;
   }

   public boolean isLeftHanded() {
      return (this.entityData.get(DATA_MOB_FLAGS_ID) & 2) != 0;
   }

   public boolean isAggressive() {
      return (this.entityData.get(DATA_MOB_FLAGS_ID) & 4) != 0;
   }

   public void setBaby(boolean pBaby) {
   }

   public HumanoidArm getMainArm() {
      return this.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
   }

   public double getMeleeAttackRangeSqr(LivingEntity pEntity) {
      return (double)(this.getBbWidth() * 2.0F * this.getBbWidth() * 2.0F + pEntity.getBbWidth());
   }

   public double getPerceivedTargetDistanceSquareForMeleeAttack(LivingEntity pEntity) {
      return Math.max(this.distanceToSqr(pEntity.getMeleeAttackReferencePosition()), this.distanceToSqr(pEntity.position()));
   }

   public boolean isWithinMeleeAttackRange(LivingEntity pEntity) {
      double d0 = this.getPerceivedTargetDistanceSquareForMeleeAttack(pEntity);
      return d0 <= this.getMeleeAttackRangeSqr(pEntity);
   }

   public boolean doHurtTarget(Entity pEntity) {
      float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
      float f1 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
      if (pEntity instanceof LivingEntity) {
         f += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity)pEntity).getMobType());
         f1 += (float)EnchantmentHelper.getKnockbackBonus(this);
      }

      int i = EnchantmentHelper.getFireAspect(this);
      if (i > 0) {
         pEntity.setSecondsOnFire(i * 4);
      }

      boolean flag = pEntity.hurt(this.damageSources().mobAttack(this), f);
      if (flag) {
         if (f1 > 0.0F && pEntity instanceof LivingEntity) {
            ((LivingEntity)pEntity).knockback((double)(f1 * 0.5F), (double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))));
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
         }

         if (pEntity instanceof Player) {
            Player player = (Player)pEntity;
            this.maybeDisableShield(player, this.getMainHandItem(), player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY);
         }

         this.doEnchantDamageEffects(this, pEntity);
         this.setLastHurtMob(pEntity);
      }

      return flag;
   }

   private void maybeDisableShield(Player pPlayer, ItemStack pMobItemStack, ItemStack pPlayerItemStack) {
      if (!pMobItemStack.isEmpty() && !pPlayerItemStack.isEmpty() && pMobItemStack.getItem() instanceof AxeItem && pPlayerItemStack.is(Items.SHIELD)) {
         float f = 0.25F + (float)EnchantmentHelper.getBlockEfficiency(this) * 0.05F;
         if (this.random.nextFloat() < f) {
            pPlayer.getCooldowns().addCooldown(Items.SHIELD, 100);
            this.level().broadcastEntityEvent(pPlayer, (byte)30);
         }
      }

   }

   protected boolean isSunBurnTick() {
      if (this.level().isDay() && !this.level().isClientSide) {
         float f = this.getLightLevelDependentMagicValue();
         BlockPos blockpos = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
         boolean flag = this.isInWaterRainOrBubble() || this.isInPowderSnow || this.wasInPowderSnow;
         if (f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && !flag && this.level().canSeeSky(blockpos)) {
            return true;
         }
      }

      return false;
   }

   protected void jumpInLiquid(TagKey<Fluid> pFluidTag) {
      this.jumpInLiquidInternal(() -> {
         super.jumpInLiquid(pFluidTag);
      });
   }

   private void jumpInLiquidInternal(Runnable onSuper) {
      if (this.getNavigation().canFloat()) {
         onSuper.run();
      } else {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.3D, 0.0D));
      }

   }

   public void jumpInFluid(FluidType type) {
      this.jumpInLiquidInternal(() -> {
         IForgeLivingEntity.super.jumpInFluid(type);
      });
   }

   public final MobSpawnType getSpawnType() {
      return this.spawnType;
   }

   public final boolean isSpawnCancelled() {
      return this.spawnCancelled;
   }

   public final void setSpawnCancelled(boolean cancel) {
      if (this.isAddedToWorld()) {
         throw new UnsupportedOperationException("Late invocations of Mob#setSpawnCancelled are not permitted.");
      } else {
         this.spawnCancelled = cancel;
      }
   }

   public void removeFreeWill() {
      this.removeAllGoals((goalIn) -> {
         return true;
      });
      this.getBrain().removeAllBehaviors();
   }

   public void removeAllGoals(Predicate<Goal> pFilter) {
      this.goalSelector.removeAllGoals(pFilter);
   }

   protected void removeAfterChangingDimensions() {
      super.removeAfterChangingDimensions();
      this.dropLeash(true, false);
      this.getAllSlots().forEach((p_278552_0_) -> {
         if (!p_278552_0_.isEmpty()) {
            p_278552_0_.setCount(0);
         }

      });
   }

   @Nullable
   public ItemStack getPickResult() {
      SpawnEggItem spawneggitem = SpawnEggItem.byId(this.getType());
      return spawneggitem == null ? null : new ItemStack(spawneggitem);
   }

   private boolean canSkipUpdate() {
      if (this.isBaby()) {
         return false;
      } else if (this.hurtTime > 0) {
         return false;
      } else if (this.tickCount < 20) {
         return false;
      } else {
         List list = this.getListPlayers(this.getCommandSenderWorld());
         if (list == null) {
            return false;
         } else if (list.size() != 1) {
            return false;
         } else {
            Entity entity = (Entity)list.get(0);
            double d0 = Math.max(Math.abs(this.getX() - entity.getX()) - 16.0D, 0.0D);
            double d1 = Math.max(Math.abs(this.getZ() - entity.getZ()) - 16.0D, 0.0D);
            double d2 = d0 * d0 + d1 * d1;
            return !this.shouldRenderAtSqrDistance(d2);
         }
      }
   }

   private List getListPlayers(Level entityWorld) {
      Level level = this.getCommandSenderWorld();
      if (level instanceof ClientLevel clientlevel) {
         return clientlevel.players();
      } else if (level instanceof ServerLevel serverlevel) {
         return serverlevel.players();
      } else {
         return null;
      }
   }

   private void onUpdateMinimal() {
      ++this.noActionTime;
      if (this instanceof Monster) {
         float f = this.getLightLevelDependentMagicValue();
         boolean flag = this instanceof Raider;
         if (f > 0.5F || flag) {
            this.noActionTime += 2;
         }
      }

   }
}
