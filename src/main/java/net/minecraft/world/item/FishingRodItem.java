package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class FishingRodItem extends Item implements Vanishable {
   public FishingRodItem(Item.Properties pProperties) {
      super(pProperties);
   }

   public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (pPlayer.fishing != null) {
         if (!pLevel.isClientSide) {
            int i = pPlayer.fishing.retrieve(itemstack);
            itemstack.hurtAndBreak(i, pPlayer, (p_41288_) -> {
               p_41288_.broadcastBreakEvent(pHand);
            });
         }

         pLevel.playSound((Player)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
         pPlayer.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
      } else {
         pLevel.playSound((Player)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
         if (!pLevel.isClientSide) {
            int k = EnchantmentHelper.getFishingSpeedBonus(itemstack);
            int j = EnchantmentHelper.getFishingLuckBonus(itemstack);
            pLevel.addFreshEntity(new FishingHook(pPlayer, pLevel, j, k));
         }

         pPlayer.awardStat(Stats.ITEM_USED.get(this));
         pPlayer.gameEvent(GameEvent.ITEM_INTERACT_START);
      }

      return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
   }

   public int getEnchantmentValue() {
      return 1;
   }
}