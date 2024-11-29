package net.minecraft.world.level.block;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class HugeMushroomBlock extends Block {
   public static final BooleanProperty NORTH = PipeBlock.NORTH;
   public static final BooleanProperty EAST = PipeBlock.EAST;
   public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
   public static final BooleanProperty WEST = PipeBlock.WEST;
   public static final BooleanProperty UP = PipeBlock.UP;
   public static final BooleanProperty DOWN = PipeBlock.DOWN;
   private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;

   public HugeMushroomBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, Boolean.valueOf(true)).setValue(EAST, Boolean.valueOf(true)).setValue(SOUTH, Boolean.valueOf(true)).setValue(WEST, Boolean.valueOf(true)).setValue(UP, Boolean.valueOf(true)).setValue(DOWN, Boolean.valueOf(true)));
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockGetter blockgetter = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      return this.defaultBlockState().setValue(DOWN, Boolean.valueOf(!blockgetter.getBlockState(blockpos.below()).is(this))).setValue(UP, Boolean.valueOf(!blockgetter.getBlockState(blockpos.above()).is(this))).setValue(NORTH, Boolean.valueOf(!blockgetter.getBlockState(blockpos.north()).is(this))).setValue(EAST, Boolean.valueOf(!blockgetter.getBlockState(blockpos.east()).is(this))).setValue(SOUTH, Boolean.valueOf(!blockgetter.getBlockState(blockpos.south()).is(this))).setValue(WEST, Boolean.valueOf(!blockgetter.getBlockState(blockpos.west()).is(this)));
   }

   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pFacingState.is(this) ? pState.setValue(PROPERTY_BY_DIRECTION.get(pFacing), Boolean.valueOf(false)) : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public BlockState rotate(BlockState pState, Rotation pRot) {
      return pState.setValue(PROPERTY_BY_DIRECTION.get(pRot.rotate(Direction.NORTH)), pState.getValue(NORTH)).setValue(PROPERTY_BY_DIRECTION.get(pRot.rotate(Direction.SOUTH)), pState.getValue(SOUTH)).setValue(PROPERTY_BY_DIRECTION.get(pRot.rotate(Direction.EAST)), pState.getValue(EAST)).setValue(PROPERTY_BY_DIRECTION.get(pRot.rotate(Direction.WEST)), pState.getValue(WEST)).setValue(PROPERTY_BY_DIRECTION.get(pRot.rotate(Direction.UP)), pState.getValue(UP)).setValue(PROPERTY_BY_DIRECTION.get(pRot.rotate(Direction.DOWN)), pState.getValue(DOWN));
   }

   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.setValue(PROPERTY_BY_DIRECTION.get(pMirror.mirror(Direction.NORTH)), pState.getValue(NORTH)).setValue(PROPERTY_BY_DIRECTION.get(pMirror.mirror(Direction.SOUTH)), pState.getValue(SOUTH)).setValue(PROPERTY_BY_DIRECTION.get(pMirror.mirror(Direction.EAST)), pState.getValue(EAST)).setValue(PROPERTY_BY_DIRECTION.get(pMirror.mirror(Direction.WEST)), pState.getValue(WEST)).setValue(PROPERTY_BY_DIRECTION.get(pMirror.mirror(Direction.UP)), pState.getValue(UP)).setValue(PROPERTY_BY_DIRECTION.get(pMirror.mirror(Direction.DOWN)), pState.getValue(DOWN));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
   }
}