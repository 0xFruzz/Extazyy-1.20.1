package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SpyglassItem extends Item {
   public static final int USE_DURATION = 1200;
   public static final float ZOOM_FOV_MODIFIER = 0.1F;

   public SpyglassItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public int getUseDuration(ItemStack pStack) {
      return 1200;
   }

   public UseAnim getUseAnimation(ItemStack pStack) {
      return UseAnim.SPYGLASS;
   }

   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
      pPlayer.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
      pPlayer.awardStat(Stats.ITEM_USED.get(this));
      return ItemUtils.startUsingInstantly(pLevel, pPlayer, pUsedHand);
   }

   public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
      this.stopUsing(pLivingEntity);
      return pStack;
   }

   public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
      this.stopUsing(pLivingEntity);
   }

   private void stopUsing(LivingEntity pUser) {
      pUser.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0F, 1.0F);
   }
}