package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ConcretePowderBlock extends FallingBlock {
   private final BlockState concrete;

   public ConcretePowderBlock(Block pConcrete, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.concrete = pConcrete.defaultBlockState();
   }

   public void onLand(Level pLevel, BlockPos pPos, BlockState pState, BlockState pReplaceableState, FallingBlockEntity pFallingBlock) {
      if (shouldSolidify(pLevel, pPos, pReplaceableState)) {
         pLevel.setBlock(pPos, this.concrete, 3);
      }

   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockGetter blockgetter = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = blockgetter.getBlockState(blockpos);
      return shouldSolidify(blockgetter, blockpos, blockstate) ? this.concrete : super.getStateForPlacement(pContext);
   }

   private static boolean shouldSolidify(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      return canSolidify(pState) || touchesLiquid(pLevel, pPos);
   }

   private static boolean touchesLiquid(BlockGetter pLevel, BlockPos pPos) {
      boolean flag = false;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();

      for(Direction direction : Direction.values()) {
         BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos);
         if (direction != Direction.DOWN || canSolidify(blockstate)) {
            blockpos$mutableblockpos.setWithOffset(pPos, direction);
            blockstate = pLevel.getBlockState(blockpos$mutableblockpos);
            if (canSolidify(blockstate) && !blockstate.isFaceSturdy(pLevel, pPos, direction.getOpposite())) {
               flag = true;
               break;
            }
         }
      }

      return flag;
   }

   private static boolean canSolidify(BlockState pState) {
      return pState.getFluidState().is(FluidTags.WATER);
   }

   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return touchesLiquid(pLevel, pCurrentPos) ? this.concrete : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public int getDustColor(BlockState pState, BlockGetter pReader, BlockPos pPos) {
      return pState.getMapColor(pReader, pPos).col;
   }
}