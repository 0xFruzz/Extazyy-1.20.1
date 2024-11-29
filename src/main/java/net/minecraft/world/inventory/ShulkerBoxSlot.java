package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ShulkerBoxSlot extends Slot {
   public ShulkerBoxSlot(Container pContainer, int pSlot, int pX, int pY) {
      super(pContainer, pSlot, pX, pY);
   }

   public boolean mayPlace(ItemStack pStack) {
      return pStack.getItem().canFitInsideContainerItems();
   }
}