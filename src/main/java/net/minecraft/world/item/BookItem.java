package net.minecraft.world.item;

public class BookItem extends Item {
   public BookItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public boolean isEnchantable(ItemStack pStack) {
      return pStack.getCount() == 1;
   }

   public int getEnchantmentValue() {
      return 1;
   }
}