package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class MendingEnchantment extends Enchantment {
   public MendingEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.BREAKABLE, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return pEnchantmentLevel * 25;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 50;
   }

   public boolean isTreasureOnly() {
      return true;
   }
}