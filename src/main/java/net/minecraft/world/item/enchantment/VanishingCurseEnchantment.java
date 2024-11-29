package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class VanishingCurseEnchantment extends Enchantment {
   public VanishingCurseEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.VANISHABLE, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return 25;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return 50;
   }

   public boolean isTreasureOnly() {
      return true;
   }

   public boolean isCurse() {
      return true;
   }
}