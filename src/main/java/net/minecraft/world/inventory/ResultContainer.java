package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class ResultContainer implements Container, RecipeHolder {
   private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(1, ItemStack.EMPTY);
   @Nullable
   private Recipe<?> recipeUsed;

   public int getContainerSize() {
      return 1;
   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.itemStacks) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int pIndex) {
      return this.itemStacks.get(0);
   }

   public ItemStack removeItem(int pIndex, int pCount) {
      return ContainerHelper.takeItem(this.itemStacks, 0);
   }

   public ItemStack removeItemNoUpdate(int pIndex) {
      return ContainerHelper.takeItem(this.itemStacks, 0);
   }

   public void setItem(int pIndex, ItemStack pStack) {
      this.itemStacks.set(0, pStack);
   }

   public void setChanged() {
   }

   public boolean stillValid(Player pPlayer) {
      return true;
   }

   public void clearContent() {
      this.itemStacks.clear();
   }

   public void setRecipeUsed(@Nullable Recipe<?> pRecipe) {
      this.recipeUsed = pRecipe;
   }

   @Nullable
   public Recipe<?> getRecipeUsed() {
      return this.recipeUsed;
   }
}