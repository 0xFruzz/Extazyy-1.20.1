package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;

public class TridentImpalerEnchantment extends Enchantment {
   public TridentImpalerEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.TRIDENT, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return 1 + (pEnchantmentLevel - 1) * 8;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 20;
   }

   public int getMaxLevel() {
      return 5;
   }

   public float getDamageBonus(int pLevel, MobType pCreatureType) {
      return pCreatureType == MobType.WATER ? (float)pLevel * 2.5F : 0.0F;
   }
}