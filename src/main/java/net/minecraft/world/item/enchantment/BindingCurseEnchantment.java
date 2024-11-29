package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BindingCurseEnchantment extends Enchantment {
   public BindingCurseEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.WEARABLE, pApplicableSlots);
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

   public boolean canEnchant(ItemStack pStack) {
      return !pStack.is(Items.SHIELD) && super.canEnchant(pStack);
   }
}