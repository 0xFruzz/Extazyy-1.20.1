package net.minecraft.world.level.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StemBlock extends BushBlock implements BonemealableBlock {
   public static final int MAX_AGE = 7;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
   protected static final float AABB_OFFSET = 1.0F;
   protected static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{Block.box(7.0D, 0.0D, 7.0D, 9.0D, 2.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 4.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 6.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 8.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 12.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 14.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D)};
   private final StemGrownBlock fruit;
   private final Supplier<Item> seedSupplier;

   protected StemBlock(StemGrownBlock pFruit, Supplier<Item> pSeedSupplier, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.fruit = pFruit;
      this.seedSupplier = pSeedSupplier;
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE_BY_AGE[pState.getValue(AGE)];
   }

   protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return pState.is(Blocks.FARMLAND);
   }

   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pLevel.getRawBrightness(pPos, 0) >= 9) {
         float f = CropBlock.getGrowthSpeed(this, pLevel, pPos);
         if (pRandom.nextInt((int)(25.0F / f) + 1) == 0) {
            int i = pState.getValue(AGE);
            if (i < 7) {
               pState = pState.setValue(AGE, Integer.valueOf(i + 1));
               pLevel.setBlock(pPos, pState, 2);
            } else {
               Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(pRandom);
               BlockPos blockpos = pPos.relative(direction);
               BlockState blockstate = pLevel.getBlockState(blockpos.below());
               if (pLevel.getBlockState(blockpos).isAir() && (blockstate.is(Blocks.FARMLAND) || blockstate.is(BlockTags.DIRT))) {
                  pLevel.setBlockAndUpdate(blockpos, this.fruit.defaultBlockState());
                  pLevel.setBlockAndUpdate(pPos, this.fruit.getAttachedStem().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction));
               }
            }
         }

      }
   }

   public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      return new ItemStack(this.seedSupplier.get());
   }

   public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      return pState.getValue(AGE) != 7;
   }

   public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      return true;
   }

   public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      int i = Math.min(7, pState.getValue(AGE) + Mth.nextInt(pLevel.random, 2, 5));
      BlockState blockstate = pState.setValue(AGE, Integer.valueOf(i));
      pLevel.setBlock(pPos, blockstate, 2);
      if (i == 7) {
         blockstate.randomTick(pLevel, pPos, pLevel.random);
      }

   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(AGE);
   }

   public StemGrownBlock getFruit() {
      return this.fruit;
   }
}