package net.minecraft.world.ticks;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public interface ContainerSingleItem extends Container {
   default int getContainerSize() {
      return 1;
   }

   default boolean isEmpty() {
      return this.getFirstItem().isEmpty();
   }

   default void clearContent() {
      this.removeFirstItem();
   }

   default ItemStack getFirstItem() {
      return this.getItem(0);
   }

   default ItemStack removeFirstItem() {
      return this.removeItemNoUpdate(0);
   }

   default void setFirstItem(ItemStack pItem) {
      this.setItem(0, pItem);
   }

   default ItemStack removeItemNoUpdate(int pSlot) {
      return this.removeItem(pSlot, this.getMaxStackSize());
   }
}