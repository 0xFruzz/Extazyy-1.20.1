package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class SwiftSneakEnchantment extends Enchantment {
   public SwiftSneakEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.ARMOR_LEGS, pApplicableSlots);
   }

   public int getMinCost(int pLevel) {
      return pLevel * 25;
   }

   public int getMaxCost(int pLevel) {
      return this.getMinCost(pLevel) + 50;
   }

   public boolean isTreasureOnly() {
      return true;
   }

   public boolean isTradeable() {
      return false;
   }

   public boolean isDiscoverable() {
      return false;
   }

   public int getMaxLevel() {
      return 3;
   }
}