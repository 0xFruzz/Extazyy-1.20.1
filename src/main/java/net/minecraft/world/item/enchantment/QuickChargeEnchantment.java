package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class QuickChargeEnchantment extends Enchantment {
   public QuickChargeEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.CROSSBOW, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return 12 + (pEnchantmentLevel - 1) * 20;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return 50;
   }

   public int getMaxLevel() {
      return 3;
   }
}