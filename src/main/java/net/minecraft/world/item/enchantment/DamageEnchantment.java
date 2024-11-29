package net.minecraft.world.item.enchantment;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;

public class DamageEnchantment extends Enchantment {
   public static final int ALL = 0;
   public static final int UNDEAD = 1;
   public static final int ARTHROPODS = 2;
   private static final String[] NAMES = new String[]{"all", "undead", "arthropods"};
   private static final int[] MIN_COST = new int[]{1, 5, 5};
   private static final int[] LEVEL_COST = new int[]{11, 8, 8};
   private static final int[] LEVEL_COST_SPAN = new int[]{20, 20, 20};
   public final int type;

   public DamageEnchantment(Enchantment.Rarity pRarity, int pType, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.WEAPON, pApplicableSlots);
      this.type = pType;
   }

   public int getMinCost(int pEnchantmentLevel) {
      return MIN_COST[this.type] + (pEnchantmentLevel - 1) * LEVEL_COST[this.type];
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + LEVEL_COST_SPAN[this.type];
   }

   public int getMaxLevel() {
      return 5;
   }

   public float getDamageBonus(int pLevel, MobType pCreatureType) {
      if (this.type == 0) {
         return 1.0F + (float)Math.max(0, pLevel - 1) * 0.5F;
      } else if (this.type == 1 && pCreatureType == MobType.UNDEAD) {
         return (float)pLevel * 2.5F;
      } else {
         return this.type == 2 && pCreatureType == MobType.ARTHROPOD ? (float)pLevel * 2.5F : 0.0F;
      }
   }

   public boolean checkCompatibility(Enchantment pEnch) {
      return !(pEnch instanceof DamageEnchantment);
   }

   public boolean canEnchant(ItemStack pStack) {
      return pStack.getItem() instanceof AxeItem ? true : super.canEnchant(pStack);
   }

   public void doPostAttack(LivingEntity pUser, Entity pTarget, int pLevel) {
      if (pTarget instanceof LivingEntity livingentity) {
         if (this.type == 2 && pLevel > 0 && livingentity.getMobType() == MobType.ARTHROPOD) {
            int i = 20 + pUser.getRandom().nextInt(10 * pLevel);
            livingentity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, i, 3));
         }
      }

   }
}