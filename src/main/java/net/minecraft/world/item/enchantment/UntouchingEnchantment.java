package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;

public class UntouchingEnchantment extends Enchantment {
   protected UntouchingEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.DIGGER, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return 15;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return super.getMinCost(pEnchantmentLevel) + 50;
   }

   public boolean checkCompatibility(Enchantment pEnch) {
      return super.checkCompatibility(pEnch) && pEnch != Enchantments.BLOCK_FORTUNE;
   }
}