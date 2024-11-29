package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class ResultSlot extends Slot {
   private final CraftingContainer craftSlots;
   private final Player player;
   private int removeCount;

   public ResultSlot(Player pPlayer, CraftingContainer pCraftSlots, Container pContainer, int pSlot, int pXPosition, int pYPosition) {
      super(pContainer, pSlot, pXPosition, pYPosition);
      this.player = pPlayer;
      this.craftSlots = pCraftSlots;
   }

   public boolean mayPlace(ItemStack pStack) {
      return false;
   }

   public ItemStack remove(int pAmount) {
      if (this.hasItem()) {
         this.removeCount += Math.min(pAmount, this.getItem().getCount());
      }

      return super.remove(pAmount);
   }

   protected void onQuickCraft(ItemStack pStack, int pAmount) {
      this.removeCount += pAmount;
      this.checkTakeAchievements(pStack);
   }

   protected void onSwapCraft(int pNumItemsCrafted) {
      this.removeCount += pNumItemsCrafted;
   }

   protected void checkTakeAchievements(ItemStack pStack) {
      if (this.removeCount > 0) {
         pStack.onCraftedBy(this.player.level(), this.player, this.removeCount);
      }

      Container container = this.container;
      if (container instanceof RecipeHolder recipeholder) {
         recipeholder.awardUsedRecipes(this.player, this.craftSlots.getItems());
      }

      this.removeCount = 0;
   }

   public void onTake(Player pPlayer, ItemStack pStack) {
      this.checkTakeAchievements(pStack);
      NonNullList<ItemStack> nonnulllist = pPlayer.level().getRecipeManager().getRemainingItemsFor(RecipeType.CRAFTING, this.craftSlots, pPlayer.level());

      for(int i = 0; i < nonnulllist.size(); ++i) {
         ItemStack itemstack = this.craftSlots.getItem(i);
         ItemStack itemstack1 = nonnulllist.get(i);
         if (!itemstack.isEmpty()) {
            this.craftSlots.removeItem(i, 1);
            itemstack = this.craftSlots.getItem(i);
         }

         if (!itemstack1.isEmpty()) {
            if (itemstack.isEmpty()) {
               this.craftSlots.setItem(i, itemstack1);
            } else if (ItemStack.isSameItemSameTags(itemstack, itemstack1)) {
               itemstack1.grow(itemstack.getCount());
               this.craftSlots.setItem(i, itemstack1);
            } else if (!this.player.getInventory().add(itemstack1)) {
               this.player.drop(itemstack1, false);
            }
         }
      }

   }
}