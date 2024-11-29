package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DiggingEnchantment extends Enchantment {
   protected DiggingEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.DIGGER, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return 1 + 10 * (pEnchantmentLevel - 1);
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return super.getMinCost(pEnchantmentLevel) + 50;
   }

   public int getMaxLevel() {
      return 5;
   }

   public boolean canEnchant(ItemStack pStack) {
      return pStack.is(Items.SHEARS) ? true : super.canEnchant(pStack);
   }
}