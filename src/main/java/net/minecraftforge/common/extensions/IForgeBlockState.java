package net.minecraftforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface IForgeBlockState {
   private BlockState self() {
      return (BlockState)this;
   }

   default int getLightEmission(BlockGetter level, BlockPos pos) {
      return this.self().getLightEmission();
   }
}