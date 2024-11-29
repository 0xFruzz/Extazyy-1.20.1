package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DropExperienceBlock extends Block {
   private final IntProvider xpRange;

   public DropExperienceBlock(BlockBehaviour.Properties pProperties) {
      this(pProperties, ConstantInt.of(0));
   }

   public DropExperienceBlock(BlockBehaviour.Properties pProperties, IntProvider pXpRange) {
      super(pProperties);
      this.xpRange = pXpRange;
   }

   public void spawnAfterBreak(BlockState pState, ServerLevel pLevel, BlockPos pPos, ItemStack pStack, boolean pDropExperience) {
      super.spawnAfterBreak(pState, pLevel, pPos, pStack, pDropExperience);
      if (pDropExperience) {
         this.tryDropExperience(pLevel, pPos, pStack, this.xpRange);
      }

   }
}