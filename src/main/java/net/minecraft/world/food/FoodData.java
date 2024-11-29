package net.minecraft.world.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;

public class FoodData {
   private int foodLevel = 20;
   private float saturationLevel;
   private float exhaustionLevel;
   private int tickTimer;
   private int lastFoodLevel = 20;

   public FoodData() {
      this.saturationLevel = 5.0F;
   }

   public void eat(int pFoodLevelModifier, float pSaturationLevelModifier) {
      this.foodLevel = Math.min(pFoodLevelModifier + this.foodLevel, 20);
      this.saturationLevel = Math.min(this.saturationLevel + (float)pFoodLevelModifier * pSaturationLevelModifier * 2.0F, (float)this.foodLevel);
   }

   public void eat(Item pItem, ItemStack pStack) {
      if (pItem.isEdible()) {
         FoodProperties foodproperties = pItem.getFoodProperties();
         this.eat(foodproperties.getNutrition(), foodproperties.getSaturationModifier());
      }

   }

   public void tick(Player pPlayer) {
      Difficulty difficulty = pPlayer.level().getDifficulty();
      this.lastFoodLevel = this.foodLevel;
      if (this.exhaustionLevel > 4.0F) {
         this.exhaustionLevel -= 4.0F;
         if (this.saturationLevel > 0.0F) {
            this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);
         } else if (difficulty != Difficulty.PEACEFUL) {
            this.foodLevel = Math.max(this.foodLevel - 1, 0);
         }
      }

      boolean flag = pPlayer.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
      if (flag && this.saturationLevel > 0.0F && pPlayer.isHurt() && this.foodLevel >= 20) {
         ++this.tickTimer;
         if (this.tickTimer >= 10) {
            float f = Math.min(this.saturationLevel, 6.0F);
            pPlayer.heal(f / 6.0F);
            this.addExhaustion(f);
            this.tickTimer = 0;
         }
      } else if (flag && this.foodLevel >= 18 && pPlayer.isHurt()) {
         ++this.tickTimer;
         if (this.tickTimer >= 80) {
            pPlayer.heal(1.0F);
            this.addExhaustion(6.0F);
            this.tickTimer = 0;
         }
      } else if (this.foodLevel <= 0) {
         ++this.tickTimer;
         if (this.tickTimer >= 80) {
            if (pPlayer.getHealth() > 10.0F || difficulty == Difficulty.HARD || pPlayer.getHealth() > 1.0F && difficulty == Difficulty.NORMAL) {
               pPlayer.hurt(pPlayer.damageSources().starve(), 1.0F);
            }

            this.tickTimer = 0;
         }
      } else {
         this.tickTimer = 0;
      }

   }

   public void readAdditionalSaveData(CompoundTag pCompoundTag) {
      if (pCompoundTag.contains("foodLevel", 99)) {
         this.foodLevel = pCompoundTag.getInt("foodLevel");
         this.tickTimer = pCompoundTag.getInt("foodTickTimer");
         this.saturationLevel = pCompoundTag.getFloat("foodSaturationLevel");
         this.exhaustionLevel = pCompoundTag.getFloat("foodExhaustionLevel");
      }

   }

   public void addAdditionalSaveData(CompoundTag pCompoundTag) {
      pCompoundTag.putInt("foodLevel", this.foodLevel);
      pCompoundTag.putInt("foodTickTimer", this.tickTimer);
      pCompoundTag.putFloat("foodSaturationLevel", this.saturationLevel);
      pCompoundTag.putFloat("foodExhaustionLevel", this.exhaustionLevel);
   }

   public int getFoodLevel() {
      return this.foodLevel;
   }

   public int getLastFoodLevel() {
      return this.lastFoodLevel;
   }

   public boolean needsFood() {
      return this.foodLevel < 20;
   }

   public void addExhaustion(float pExhaustion) {
      this.exhaustionLevel = Math.min(this.exhaustionLevel + pExhaustion, 40.0F);
   }

   public float getExhaustionLevel() {
      return this.exhaustionLevel;
   }

   public float getSaturationLevel() {
      return this.saturationLevel;
   }

   public void setFoodLevel(int pFoodLevel) {
      this.foodLevel = pFoodLevel;
   }

   public void setSaturation(float pSaturationLevel) {
      this.saturationLevel = pSaturationLevel;
   }

   public void setExhaustion(float pExhaustionLevel) {
      this.exhaustionLevel = pExhaustionLevel;
   }
}