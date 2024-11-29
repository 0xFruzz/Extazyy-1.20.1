package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class WaterWalkerEnchantment extends Enchantment {
   public WaterWalkerEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.ARMOR_FEET, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return pEnchantmentLevel * 10;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return this.getMinCost(pEnchantmentLevel) + 15;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean checkCompatibility(Enchantment pEnch) {
      return super.checkCompatibility(pEnch) && pEnch != Enchantments.FROST_WALKER;
   }
}