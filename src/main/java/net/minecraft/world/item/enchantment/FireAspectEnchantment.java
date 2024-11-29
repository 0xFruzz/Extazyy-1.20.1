package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class FireAspectEnchantment extends Enchantment {
   protected FireAspectEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.WEAPON, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return 10 + 20 * (pEnchantmentLevel - 1);
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return super.getMinCost(pEnchantmentLevel) + 50;
   }

   public int getMaxLevel() {
      return 2;
   }
}