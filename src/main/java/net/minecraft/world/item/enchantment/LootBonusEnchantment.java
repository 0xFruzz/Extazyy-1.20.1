package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class LootBonusEnchantment extends Enchantment {
   protected LootBonusEnchantment(Enchantment.Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot... pApplicableSlots) {
      super(pRarity, pCategory, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return 15 + (pEnchantmentLevel - 1) * 9;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return super.getMinCost(pEnchantmentLevel) + 50;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean checkCompatibility(Enchantment pEnch) {
      return super.checkCompatibility(pEnch) && pEnch != Enchantments.SILK_TOUCH;
   }
}