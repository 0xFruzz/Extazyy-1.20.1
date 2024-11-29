package net.minecraft.world.item;

public class EnchantedGoldenAppleItem extends Item {
   public EnchantedGoldenAppleItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public boolean isFoil(ItemStack pStack) {
      return true;
   }
}