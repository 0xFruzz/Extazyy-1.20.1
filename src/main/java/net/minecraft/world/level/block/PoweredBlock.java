package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PoweredBlock extends Block {
   public PoweredBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public boolean isSignalSource(BlockState pState) {
      return true;
   }

   public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
      return 15;
   }
}