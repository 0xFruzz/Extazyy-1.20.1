package net.minecraft.world.entity.monster;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;

public abstract class Monster extends PathfinderMob implements Enemy {
   protected Monster(EntityType<? extends Monster> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.xpReward = 5;
   }

   public SoundSource getSoundSource() {
      return SoundSource.HOSTILE;
   }

   public void aiStep() {
      this.updateSwingTime();
      this.updateNoActionTime();
      super.aiStep();
   }

   protected void updateNoActionTime() {
      float f = this.getLightLevelDependentMagicValue();
      if (f > 0.5F) {
         this.noActionTime += 2;
      }

   }

   protected boolean shouldDespawnInPeaceful() {
      return true;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.HOSTILE_SWIM;
   }

   protected SoundEvent getSwimSplashSound() {
      return SoundEvents.HOSTILE_SPLASH;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.HOSTILE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.HOSTILE_DEATH;
   }

   public LivingEntity.Fallsounds getFallSounds() {
      return new LivingEntity.Fallsounds(SoundEvents.HOSTILE_SMALL_FALL, SoundEvents.HOSTILE_BIG_FALL);
   }

   public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
      return -pLevel.getPathfindingCostFromLightLevels(pPos);
   }

   public static boolean isDarkEnoughToSpawn(ServerLevelAccessor pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pLevel.getBrightness(LightLayer.SKY, pPos) > pRandom.nextInt(32)) {
         return false;
      } else {
         DimensionType dimensiontype = pLevel.dimensionType();
         int i = dimensiontype.monsterSpawnBlockLightLimit();
         if (i < 15 && pLevel.getBrightness(LightLayer.BLOCK, pPos) > i) {
            return false;
         } else {
            int j = pLevel.getLevel().isThundering() ? pLevel.getMaxLocalRawBrightness(pPos, 10) : pLevel.getMaxLocalRawBrightness(pPos);
            return j <= dimensiontype.monsterSpawnLightTest().sample(pRandom);
         }
      }
   }

   public static boolean checkMonsterSpawnRules(EntityType<? extends Monster> pType, ServerLevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      return pLevel.getDifficulty() != Difficulty.PEACEFUL && isDarkEnoughToSpawn(pLevel, pPos, pRandom) && checkMobSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom);
   }

   public static boolean checkAnyLightMonsterSpawnRules(EntityType<? extends Monster> pType, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      return pLevel.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom);
   }

   public static AttributeSupplier.Builder createMonsterAttributes() {
      return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE);
   }

   public boolean shouldDropExperience() {
      return true;
   }

   protected boolean shouldDropLoot() {
      return true;
   }

   public boolean isPreventingPlayerRest(Player pPlayer) {
      return true;
   }

   public ItemStack getProjectile(ItemStack pShootable) {
      if (pShootable.getItem() instanceof ProjectileWeaponItem) {
         Predicate<ItemStack> predicate = ((ProjectileWeaponItem)pShootable.getItem()).getSupportedHeldProjectiles();
         ItemStack itemstack = ProjectileWeaponItem.getHeldProjectile(this, predicate);
         return itemstack.isEmpty() ? new ItemStack(Items.ARROW) : itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }
}