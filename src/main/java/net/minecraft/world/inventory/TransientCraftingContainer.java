package net.minecraft.world.inventory;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;

public class TransientCraftingContainer implements CraftingContainer {
   private final NonNullList<ItemStack> items;
   private final int width;
   private final int height;
   private final AbstractContainerMenu menu;

   public TransientCraftingContainer(AbstractContainerMenu pMenu, int pWidth, int pHeight) {
      this(pMenu, pWidth, pHeight, NonNullList.withSize(pWidth * pHeight, ItemStack.EMPTY));
   }

   public TransientCraftingContainer(AbstractContainerMenu pMenu, int pWidth, int pHeight, NonNullList<ItemStack> pItems) {
      this.items = pItems;
      this.menu = pMenu;
      this.width = pWidth;
      this.height = pHeight;
   }

   public int getContainerSize() {
      return this.items.size();
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.items) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int pSlot) {
      return pSlot >= this.getContainerSize() ? ItemStack.EMPTY : this.items.get(pSlot);
   }

   public ItemStack removeItemNoUpdate(int pSlot) {
      return ContainerHelper.takeItem(this.items, pSlot);
   }

   public ItemStack removeItem(int pSlot, int pAmount) {
      ItemStack itemstack = ContainerHelper.removeItem(this.items, pSlot, pAmount);
      if (!itemstack.isEmpty()) {
         this.menu.slotsChanged(this);
      }

      return itemstack;
   }

   public void setItem(int pSlot, ItemStack pStack) {
      this.items.set(pSlot, pStack);
      this.menu.slotsChanged(this);
   }

   public void setChanged() {
   }

   public boolean stillValid(Player pPlayer) {
      return true;
   }

   public void clearContent() {
      this.items.clear();
   }

   public int getHeight() {
      return this.height;
   }

   public int getWidth() {
      return this.width;
   }

   public List<ItemStack> getItems() {
      return List.copyOf(this.items);
   }

   public void fillStackedContents(StackedContents pContents) {
      for(ItemStack itemstack : this.items) {
         pContents.accountSimpleStack(itemstack);
      }

   }
}