package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidBlock extends Block implements BucketPickup {
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
   protected final FlowingFluid fluid;
   private final List<FluidState> stateCache;
   public static final VoxelShape STABLE_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   public static final ImmutableList<Direction> POSSIBLE_FLOW_DIRECTIONS = ImmutableList.of(Direction.DOWN, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST);

   protected LiquidBlock(FlowingFluid pFluid, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.fluid = pFluid;
      this.stateCache = Lists.newArrayList();
      this.stateCache.add(pFluid.getSource(false));

      for(int i = 1; i < 8; ++i) {
         this.stateCache.add(pFluid.getFlowing(8 - i, false));
      }

      this.stateCache.add(pFluid.getFlowing(8, true));
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return pContext.isAbove(STABLE_SHAPE, pPos, true) && pState.getValue(LEVEL) == 0 && pContext.canStandOnFluid(pLevel.getFluidState(pPos.above()), pState.getFluidState()) ? STABLE_SHAPE : Shapes.empty();
   }

   public boolean isRandomlyTicking(BlockState pState) {
      return pState.getFluidState().isRandomlyTicking();
   }

   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      pState.getFluidState().randomTick(pLevel, pPos, pRandom);
   }

   public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
      return false;
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return !this.fluid.is(FluidTags.LAVA);
   }

   public FluidState getFluidState(BlockState pState) {
      int i = pState.getValue(LEVEL);
      return this.stateCache.get(Math.min(i, 8));
   }

   public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide) {
      return pAdjacentBlockState.getFluidState().getType().isSame(this.fluid);
   }

   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.INVISIBLE;
   }

   public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
      return Collections.emptyList();
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return Shapes.empty();
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (this.shouldSpreadLiquid(pLevel, pPos, pState)) {
         pLevel.scheduleTick(pPos, pState.getFluidState().getType(), this.fluid.getTickDelay(pLevel));
      }

   }

   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pState.getFluidState().isSource() || pFacingState.getFluidState().isSource()) {
         pLevel.scheduleTick(pCurrentPos, pState.getFluidState().getType(), this.fluid.getTickDelay(pLevel));
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (this.shouldSpreadLiquid(pLevel, pPos, pState)) {
         pLevel.scheduleTick(pPos, pState.getFluidState().getType(), this.fluid.getTickDelay(pLevel));
      }

   }

   private boolean shouldSpreadLiquid(Level pLevel, BlockPos pPos, BlockState pState) {
      if (this.fluid.is(FluidTags.LAVA)) {
         boolean flag = pLevel.getBlockState(pPos.below()).is(Blocks.SOUL_SOIL);

         for(Direction direction : POSSIBLE_FLOW_DIRECTIONS) {
            BlockPos blockpos = pPos.relative(direction.getOpposite());
            if (pLevel.getFluidState(blockpos).is(FluidTags.WATER)) {
               Block block = pLevel.getFluidState(pPos).isSource() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE;
               pLevel.setBlockAndUpdate(pPos, block.defaultBlockState());
               this.fizz(pLevel, pPos);
               return false;
            }

            if (flag && pLevel.getBlockState(blockpos).is(Blocks.BLUE_ICE)) {
               pLevel.setBlockAndUpdate(pPos, Blocks.BASALT.defaultBlockState());
               this.fizz(pLevel, pPos);
               return false;
            }
         }
      }

      return true;
   }

   private void fizz(LevelAccessor pLevel, BlockPos pPos) {
      pLevel.levelEvent(1501, pPos, 0);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(LEVEL);
   }

   public ItemStack pickupBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
      if (pState.getValue(LEVEL) == 0) {
         pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 11);
         return new ItemStack(this.fluid.getBucket());
      } else {
         return ItemStack.EMPTY;
      }
   }

   public Optional<SoundEvent> getPickupSound() {
      return this.fluid.getPickupSound();
   }
}