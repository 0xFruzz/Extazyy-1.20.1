package net.minecraft.world.inventory;

import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;

public class MerchantResultSlot extends Slot {
   private final MerchantContainer slots;
   private final Player player;
   private int removeCount;
   private final Merchant merchant;

   public MerchantResultSlot(Player pPlayer, Merchant pMerchant, MerchantContainer pSlots, int pSlot, int pXPosition, int pYPosition) {
      super(pSlots, pSlot, pXPosition, pYPosition);
      this.player = pPlayer;
      this.merchant = pMerchant;
      this.slots = pSlots;
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

   protected void checkTakeAchievements(ItemStack pStack) {
      pStack.onCraftedBy(this.player.level(), this.player, this.removeCount);
      this.removeCount = 0;
   }

   public void onTake(Player pPlayer, ItemStack pStack) {
      this.checkTakeAchievements(pStack);
      MerchantOffer merchantoffer = this.slots.getActiveOffer();
      if (merchantoffer != null) {
         ItemStack itemstack = this.slots.getItem(0);
         ItemStack itemstack1 = this.slots.getItem(1);
         if (merchantoffer.take(itemstack, itemstack1) || merchantoffer.take(itemstack1, itemstack)) {
            this.merchant.notifyTrade(merchantoffer);
            pPlayer.awardStat(Stats.TRADED_WITH_VILLAGER);
            this.slots.setItem(0, itemstack);
            this.slots.setItem(1, itemstack1);
         }

         this.merchant.overrideXp(this.merchant.getVillagerXp() + merchantoffer.getXp());
      }

   }
}