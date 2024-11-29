package net.minecraft.world.item;

public class SimpleFoiledItem extends Item {
   public SimpleFoiledItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public boolean isFoil(ItemStack pStack) {
      return true;
   }
}