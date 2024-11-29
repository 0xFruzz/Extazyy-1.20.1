package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CactusBlock extends Block {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
   public static final int MAX_AGE = 15;
   protected static final int AABB_OFFSET = 1;
   protected static final VoxelShape COLLISION_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
   protected static final VoxelShape OUTLINE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

   protected CactusBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (!pState.canSurvive(pLevel, pPos)) {
         pLevel.destroyBlock(pPos, true);
      }

   }

   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      BlockPos blockpos = pPos.above();
      if (pLevel.isEmptyBlock(blockpos)) {
         int i;
         for(i = 1; pLevel.getBlockState(pPos.below(i)).is(this); ++i) {
         }

         if (i < 3) {
            int j = pState.getValue(AGE);
            if (j == 15) {
               pLevel.setBlockAndUpdate(blockpos, this.defaultBlockState());
               BlockState blockstate = pState.setValue(AGE, Integer.valueOf(0));
               pLevel.setBlock(pPos, blockstate, 4);
               pLevel.neighborChanged(blockstate, blockpos, this, pPos, false);
            } else {
               pLevel.setBlock(pPos, pState.setValue(AGE, Integer.valueOf(j + 1)), 4);
            }

         }
      }
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return COLLISION_SHAPE;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return OUTLINE_SHAPE;
   }

   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (!pState.canSurvive(pLevel, pCurrentPos)) {
         pLevel.scheduleTick(pCurrentPos, this, 1);
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockState blockstate = pLevel.getBlockState(pPos.relative(direction));
         if (blockstate.isSolid() || pLevel.getFluidState(pPos.relative(direction)).is(FluidTags.LAVA)) {
            return false;
         }
      }

      BlockState blockstate1 = pLevel.getBlockState(pPos.below());
      return (blockstate1.is(Blocks.CACTUS) || blockstate1.is(BlockTags.SAND)) && !pLevel.getBlockState(pPos.above()).liquid();
   }

   public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
      pEntity.hurt(pLevel.damageSources().cactus(), 1.0F);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(AGE);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }
}