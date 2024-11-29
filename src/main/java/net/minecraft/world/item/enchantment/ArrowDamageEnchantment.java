package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class ArrowDamageEnchantment extends Enchantment {
   public ArrowDamageEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.BOW, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return 1 + (pEnchantmentLevel - 1) * 10;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 15;
   }

   public int getMaxLevel() {
      return 5;
   }
}