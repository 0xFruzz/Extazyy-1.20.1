package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class TridentChannelingEnchantment extends Enchantment {
   public TridentChannelingEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.TRIDENT, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return 25;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return 50;
   }
}