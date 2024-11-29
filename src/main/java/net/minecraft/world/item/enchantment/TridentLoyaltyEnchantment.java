package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class TridentLoyaltyEnchantment extends Enchantment {
   public TridentLoyaltyEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.TRIDENT, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return 5 + pEnchantmentLevel * 7;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return 50;
   }

   public int getMaxLevel() {
      return 3;
   }
}