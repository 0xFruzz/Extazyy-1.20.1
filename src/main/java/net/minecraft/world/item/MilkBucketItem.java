package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MilkBucketItem extends Item {
   private static final int DRINK_DURATION = 32;

   public MilkBucketItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
      if (pEntityLiving instanceof ServerPlayer serverplayer) {
         CriteriaTriggers.CONSUME_ITEM.trigger(serverplayer, pStack);
         serverplayer.awardStat(Stats.ITEM_USED.get(this));
      }

      if (pEntityLiving instanceof Player && !((Player)pEntityLiving).getAbilities().instabuild) {
         pStack.shrink(1);
      }

      if (!pLevel.isClientSide) {
         pEntityLiving.removeAllEffects();
      }

      return pStack.isEmpty() ? new ItemStack(Items.BUCKET) : pStack;
   }

   public int getUseDuration(ItemStack pStack) {
      return 32;
   }

   public UseAnim getUseAnimation(ItemStack pStack) {
      return UseAnim.DRINK;
   }

   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
   }
}