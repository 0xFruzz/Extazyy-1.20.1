package net.minecraft.world.item.enchantment;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class DigDurabilityEnchantment extends Enchantment {
   protected DigDurabilityEnchantment(Enchantment.Rarity pRarity, EquipmentSlot... pApplicableSlots) {
      super(pRarity, EnchantmentCategory.BREAKABLE, pApplicableSlots);
   }

   public int getMinCost(int pEnchantmentLevel) {
      return 5 + (pEnchantmentLevel - 1) * 8;
   }

   public int getMaxCost(int pEnchantmentLevel) {
      return super.getMinCost(pEnchantmentLevel) + 50;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean canEnchant(ItemStack pStack) {
      return pStack.isDamageableItem() ? true : super.canEnchant(pStack);
   }

   public static boolean shouldIgnoreDurabilityDrop(ItemStack pStack, int pLevel, RandomSource pRandom) {
      if (pStack.getItem() instanceof ArmorItem && pRandom.nextFloat() < 0.6F) {
         return false;
      } else {
         return pRandom.nextInt(pLevel + 1) > 0;
      }
   }
}