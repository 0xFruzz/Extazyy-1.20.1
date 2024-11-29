package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class WaterWorkerEnchantment extends Enchantment {
   public WaterWorkerEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.ARMOR_HEAD, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return 1;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 40;
   }
}