package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class OxygenEnchantment extends Enchantment {
   public OxygenEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.ARMOR_HEAD, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return 10 * pEnchantmentLevel;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 30;
   }

   public int getMaxLevel() {
      return 3;
   }
}