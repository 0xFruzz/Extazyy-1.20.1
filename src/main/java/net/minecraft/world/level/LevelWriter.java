package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

public interface LevelWriter {
   boolean setBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft);

   default boolean setBlock(BlockPos pPos, BlockState pNewState, int pFlags) {
      return this.setBlock(pPos, pNewState, pFlags, 512);
   }

   boolean removeBlock(BlockPos pPos, boolean pIsMoving);

   default boolean destroyBlock(BlockPos pPos, boolean pDropBlock) {
      return this.destroyBlock(pPos, pDropBlock, (Entity)null);
   }

   default boolean destroyBlock(BlockPos pPos, boolean pDropBlock, @Nullable Entity pEntity) {
      return this.destroyBlock(pPos, pDropBlock, pEntity, 512);
   }

   boolean destroyBlock(BlockPos pPos, boolean pDropBlock, @Nullable Entity pEntity, int pRecursionLeft);

   default boolean addFreshEntity(Entity pEntity) {
      return false;
   }
}