package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class TridentRiptideEnchantment extends Enchantment {
   public TridentRiptideEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.TRIDENT, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return 10 + pEnchantmentLevel * 7;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return 50;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean checkCompatibility(Enchantment pEnch) {
      return super.checkCompatibility(pEnch) && pEnch != Enchantments.LOYALTY && pEnch != Enchantments.CHANNELING;
   }
}