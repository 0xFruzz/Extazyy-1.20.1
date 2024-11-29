package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface SignalGetter extends BlockGetter {
   Direction[] DIRECTIONS = Direction.values();

   default int getDirectSignal(BlockPos pPos, Direction pDirection) {
      return this.getBlockState(pPos).getDirectSignal(this, pPos, pDirection);
   }

   default int getDirectSignalTo(BlockPos pPos) {
      int i = 0;
      i = Math.max(i, this.getDirectSignal(pPos.below(), Direction.DOWN));
      if (i >= 15) {
         return i;
      } else {
         i = Math.max(i, this.getDirectSignal(pPos.above(), Direction.UP));
         if (i >= 15) {
            return i;
         } else {
            i = Math.max(i, this.getDirectSignal(pPos.north(), Direction.NORTH));
            if (i >= 15) {
               return i;
            } else {
               i = Math.max(i, this.getDirectSignal(pPos.south(), Direction.SOUTH));
               if (i >= 15) {
                  return i;
               } else {
                  i = Math.max(i, this.getDirectSignal(pPos.west(), Direction.WEST));
                  if (i >= 15) {
                     return i;
                  } else {
                     i = Math.max(i, this.getDirectSignal(pPos.east(), Direction.EAST));
                     return i >= 15 ? i : i;
                  }
               }
            }
         }
      }
   }

   default int getControlInputSignal(BlockPos pPos, Direction pDirection, boolean pDiodesOnly) {
      BlockState blockstate = this.getBlockState(pPos);
      if (pDiodesOnly) {
         return DiodeBlock.isDiode(blockstate) ? this.getDirectSignal(pPos, pDirection) : 0;
      } else if (blockstate.is(Blocks.REDSTONE_BLOCK)) {
         return 15;
      } else if (blockstate.is(Blocks.REDSTONE_WIRE)) {
         return blockstate.getValue(RedStoneWireBlock.POWER);
      } else {
         return blockstate.isSignalSource() ? this.getDirectSignal(pPos, pDirection) : 0;
      }
   }

   default boolean hasSignal(BlockPos pPos, Direction pDirection) {
      return this.getSignal(pPos, pDirection) > 0;
   }

   default int getSignal(BlockPos pPos, Direction pDirection) {
      BlockState blockstate = this.getBlockState(pPos);
      int i = blockstate.getSignal(this, pPos, pDirection);
      return blockstate.isRedstoneConductor(this, pPos) ? Math.max(i, this.getDirectSignalTo(pPos)) : i;
   }

   default boolean hasNeighborSignal(BlockPos pPos) {
      if (this.getSignal(pPos.below(), Direction.DOWN) > 0) {
         return true;
      } else if (this.getSignal(pPos.above(), Direction.UP) > 0) {
         return true;
      } else if (this.getSignal(pPos.north(), Direction.NORTH) > 0) {
         return true;
      } else if (this.getSignal(pPos.south(), Direction.SOUTH) > 0) {
         return true;
      } else if (this.getSignal(pPos.west(), Direction.WEST) > 0) {
         return true;
      } else {
         return this.getSignal(pPos.east(), Direction.EAST) > 0;
      }
   }

   default int getBestNeighborSignal(BlockPos pPos) {
      int i = 0;

      for(Direction direction : DIRECTIONS) {
         int j = this.getSignal(pPos.relative(direction), direction);
         if (j >= 15) {
            return 15;
         }

         if (j > i) {
            i = j;
         }
      }

      return i;
   }
}