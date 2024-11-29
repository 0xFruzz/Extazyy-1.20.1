package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;

public class DoublePlantBlock extends BushBlock {
   public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

   public DoublePlantBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER));
   }

   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      DoubleBlockHalf doubleblockhalf = pState.getValue(HALF);
      if (pFacing.getAxis() != Direction.Axis.Y || doubleblockhalf == DoubleBlockHalf.LOWER != (pFacing == Direction.UP) || pFacingState.is(this) && pFacingState.getValue(HALF) != doubleblockhalf) {
         return doubleblockhalf == DoubleBlockHalf.LOWER && pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      } else {
         return Blocks.AIR.defaultBlockState();
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockPos blockpos = pContext.getClickedPos();
      Level level = pContext.getLevel();
      return blockpos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(blockpos.above()).canBeReplaced(pContext) ? super.getStateForPlacement(pContext) : null;
   }

   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      BlockPos blockpos = pPos.above();
      pLevel.setBlock(blockpos, copyWaterloggedFrom(pLevel, blockpos, this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER)), 3);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      if (pState.getValue(HALF) != DoubleBlockHalf.UPPER) {
         return super.canSurvive(pState, pLevel, pPos);
      } else {
         BlockState blockstate = pLevel.getBlockState(pPos.below());
         return blockstate.is(this) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER;
      }
   }

   public static void placeAt(LevelAccessor pLevel, BlockState pState, BlockPos pPos, int pFlags) {
      BlockPos blockpos = pPos.above();
      pLevel.setBlock(pPos, copyWaterloggedFrom(pLevel, pPos, pState.setValue(HALF, DoubleBlockHalf.LOWER)), pFlags);
      pLevel.setBlock(blockpos, copyWaterloggedFrom(pLevel, blockpos, pState.setValue(HALF, DoubleBlockHalf.UPPER)), pFlags);
   }

   public static BlockState copyWaterloggedFrom(LevelReader pLevel, BlockPos pPos, BlockState pState) {
      return pState.hasProperty(BlockStateProperties.WATERLOGGED) ? pState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(pLevel.isWaterAt(pPos))) : pState;
   }

   public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      if (!pLevel.isClientSide) {
         if (pPlayer.isCreative()) {
            preventCreativeDropFromBottomPart(pLevel, pPos, pState, pPlayer);
         } else {
            dropResources(pState, pLevel, pPos, (BlockEntity)null, pPlayer, pPlayer.getMainHandItem());
         }
      }

      super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
   }

   public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @Nullable BlockEntity pTe, ItemStack pStack) {
      super.playerDestroy(pLevel, pPlayer, pPos, Blocks.AIR.defaultBlockState(), pTe, pStack);
   }

   protected static void preventCreativeDropFromBottomPart(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      DoubleBlockHalf doubleblockhalf = pState.getValue(HALF);
      if (doubleblockhalf == DoubleBlockHalf.UPPER) {
         BlockPos blockpos = pPos.below();
         BlockState blockstate = pLevel.getBlockState(blockpos);
         if (blockstate.is(pState.getBlock()) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER) {
            BlockState blockstate1 = blockstate.getFluidState().is(Fluids.WATER) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
            pLevel.setBlock(blockpos, blockstate1, 35);
            pLevel.levelEvent(pPlayer, 2001, blockpos, Block.getId(blockstate));
         }
      }

   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(HALF);
   }

   public long getSeed(BlockState pState, BlockPos pPos) {
      return Mth.getSeed(pPos.getX(), pPos.below(pState.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pPos.getZ());
   }
}