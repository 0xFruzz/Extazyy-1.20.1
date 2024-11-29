package net.minecraft.world.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public class FurnaceResultSlot extends Slot {
   private final Player player;
   private int removeCount;

   public FurnaceResultSlot(Player pPlayer, Container pContainer, int pSlot, int pXPosition, int pYPosition) {
      super(pContainer, pSlot, pXPosition, pYPosition);
      this.player = pPlayer;
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

   public void onTake(Player pPlayer, ItemStack pStack) {
      this.checkTakeAchievements(pStack);
      super.onTake(pPlayer, pStack);
   }

   protected void onQuickCraft(ItemStack pStack, int pAmount) {
      this.removeCount += pAmount;
      this.checkTakeAchievements(pStack);
   }

   protected void checkTakeAchievements(ItemStack pStack) {
      pStack.onCraftedBy(this.player.level(), this.player, this.removeCount);
      Player player = this.player;
      if (player instanceof ServerPlayer serverplayer) {
         Container container = this.container;
         if (container instanceof AbstractFurnaceBlockEntity abstractfurnaceblockentity) {
            abstractfurnaceblockentity.awardUsedRecipesAndPopExperience(serverplayer);
         }
      }

      this.removeCount = 0;
   }
}