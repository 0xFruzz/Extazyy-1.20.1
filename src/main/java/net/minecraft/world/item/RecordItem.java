package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class RecordItem extends Item {
   private static final Map<SoundEvent, RecordItem> BY_NAME = Maps.newHashMap();
   private final int analogOutput;
   private final SoundEvent sound;
   private final int lengthInTicks;

   protected RecordItem(int pAnalogOutput, SoundEvent pSound, Item.Properties pProperties, int pLengthInSeconds) {
      super(pProperties);
      this.analogOutput = pAnalogOutput;
      this.sound = pSound;
      this.lengthInTicks = pLengthInSeconds * 20;
      BY_NAME.put(this.sound, this);
   }

   public InteractionResult useOn(UseOnContext pContext) {
      Level level = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      if (blockstate.is(Blocks.JUKEBOX) && !blockstate.getValue(JukeboxBlock.HAS_RECORD)) {
         ItemStack itemstack = pContext.getItemInHand();
         if (!level.isClientSide) {
            Player player = pContext.getPlayer();
            BlockEntity blockentity = level.getBlockEntity(blockpos);
            if (blockentity instanceof JukeboxBlockEntity) {
               JukeboxBlockEntity jukeboxblockentity = (JukeboxBlockEntity)blockentity;
               jukeboxblockentity.setFirstItem(itemstack.copy());
               level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(player, blockstate));
            }

            itemstack.shrink(1);
            if (player != null) {
               player.awardStat(Stats.PLAY_RECORD);
            }
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         return InteractionResult.PASS;
      }
   }

   public int getAnalogOutput() {
      return this.analogOutput;
   }

   public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      pTooltip.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
   }

   public MutableComponent getDisplayName() {
      return Component.translatable(this.getDescriptionId() + ".desc");
   }

   @Nullable
   public static RecordItem getBySound(SoundEvent pSound) {
      return BY_NAME.get(pSound);
   }

   public SoundEvent getSound() {
      return this.sound;
   }

   public int getLengthInTicks() {
      return this.lengthInTicks;
   }
}