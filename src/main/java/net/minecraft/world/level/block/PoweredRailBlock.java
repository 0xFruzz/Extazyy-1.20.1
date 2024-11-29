package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

public class PoweredRailBlock extends BaseRailBlock {
   public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

   protected PoweredRailBlock(BlockBehaviour.Properties p_55218_) {
      super(true, p_55218_);
      this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(POWERED, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   protected boolean findPoweredRailSignal(Level pLevel, BlockPos pPos, BlockState pState, boolean pSearchForward, int pRecursionCount) {
      if (pRecursionCount >= 8) {
         return false;
      } else {
         int i = pPos.getX();
         int j = pPos.getY();
         int k = pPos.getZ();
         boolean flag = true;
         RailShape railshape = pState.getValue(SHAPE);
         switch (railshape) {
            case NORTH_SOUTH:
               if (pSearchForward) {
                  ++k;
               } else {
                  --k;
               }
               break;
            case EAST_WEST:
               if (pSearchForward) {
                  --i;
               } else {
                  ++i;
               }
               break;
            case ASCENDING_EAST:
               if (pSearchForward) {
                  --i;
               } else {
                  ++i;
                  ++j;
                  flag = false;
               }

               railshape = RailShape.EAST_WEST;
               break;
            case ASCENDING_WEST:
               if (pSearchForward) {
                  --i;
                  ++j;
                  flag = false;
               } else {
                  ++i;
               }

               railshape = RailShape.EAST_WEST;
               break;
            case ASCENDING_NORTH:
               if (pSearchForward) {
                  ++k;
               } else {
                  --k;
                  ++j;
                  flag = false;
               }

               railshape = RailShape.NORTH_SOUTH;
               break;
            case ASCENDING_SOUTH:
               if (pSearchForward) {
                  ++k;
                  ++j;
                  flag = false;
               } else {
                  --k;
               }

               railshape = RailShape.NORTH_SOUTH;
         }

         if (this.isSameRailWithPower(pLevel, new BlockPos(i, j, k), pSearchForward, pRecursionCount, railshape)) {
            return true;
         } else {
            return flag && this.isSameRailWithPower(pLevel, new BlockPos(i, j - 1, k), pSearchForward, pRecursionCount, railshape);
         }
      }
   }

   protected boolean isSameRailWithPower(Level pLevel, BlockPos pState, boolean pSearchForward, int pRecursionCount, RailShape pShape) {
      BlockState blockstate = pLevel.getBlockState(pState);
      if (!blockstate.is(this)) {
         return false;
      } else {
         RailShape railshape = blockstate.getValue(SHAPE);
         if (pShape != RailShape.EAST_WEST || railshape != RailShape.NORTH_SOUTH && railshape != RailShape.ASCENDING_NORTH && railshape != RailShape.ASCENDING_SOUTH) {
            if (pShape != RailShape.NORTH_SOUTH || railshape != RailShape.EAST_WEST && railshape != RailShape.ASCENDING_EAST && railshape != RailShape.ASCENDING_WEST) {
               if (blockstate.getValue(POWERED)) {
                  return pLevel.hasNeighborSignal(pState) ? true : this.findPoweredRailSignal(pLevel, pState, blockstate, pSearchForward, pRecursionCount + 1);
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   protected void updateState(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock) {
      boolean flag = pState.getValue(POWERED);
      boolean flag1 = pLevel.hasNeighborSignal(pPos) || this.findPoweredRailSignal(pLevel, pPos, pState, true, 0) || this.findPoweredRailSignal(pLevel, pPos, pState, false, 0);
      if (flag1 != flag) {
         pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(flag1)), 3);
         pLevel.updateNeighborsAt(pPos.below(), this);
         if (pState.getValue(SHAPE).isAscending()) {
            pLevel.updateNeighborsAt(pPos.above(), this);
         }
      }

   }

   public Property<RailShape> getShapeProperty() {
      return SHAPE;
   }

   public BlockState rotate(BlockState pState, Rotation pRot) {
      switch (pRot) {
         case CLOCKWISE_180:
            switch ((RailShape)pState.getValue(SHAPE)) {
               case ASCENDING_EAST:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_WEST:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_NORTH:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_SOUTH:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
               case SOUTH_EAST:
                  return pState.setValue(SHAPE, RailShape.NORTH_WEST);
               case SOUTH_WEST:
                  return pState.setValue(SHAPE, RailShape.NORTH_EAST);
               case NORTH_WEST:
                  return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_EAST:
                  return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
            }
         case COUNTERCLOCKWISE_90:
            switch ((RailShape)pState.getValue(SHAPE)) {
               case NORTH_SOUTH:
                  return pState.setValue(SHAPE, RailShape.EAST_WEST);
               case EAST_WEST:
                  return pState.setValue(SHAPE, RailShape.NORTH_SOUTH);
               case ASCENDING_EAST:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
               case ASCENDING_WEST:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_NORTH:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_SOUTH:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);
               case SOUTH_EAST:
                  return pState.setValue(SHAPE, RailShape.NORTH_EAST);
               case SOUTH_WEST:
                  return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_WEST:
                  return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
               case NORTH_EAST:
                  return pState.setValue(SHAPE, RailShape.NORTH_WEST);
            }
         case CLOCKWISE_90:
            switch ((RailShape)pState.getValue(SHAPE)) {
               case NORTH_SOUTH:
                  return pState.setValue(SHAPE, RailShape.EAST_WEST);
               case EAST_WEST:
                  return pState.setValue(SHAPE, RailShape.NORTH_SOUTH);
               case ASCENDING_EAST:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_WEST:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
               case ASCENDING_NORTH:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_SOUTH:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);
               case SOUTH_EAST:
                  return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
               case SOUTH_WEST:
                  return pState.setValue(SHAPE, RailShape.NORTH_WEST);
               case NORTH_WEST:
                  return pState.setValue(SHAPE, RailShape.NORTH_EAST);
               case NORTH_EAST:
                  return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
            }
         default:
            return pState;
      }
   }

   public BlockState mirror(BlockState pState, Mirror pMirror) {
      RailShape railshape = pState.getValue(SHAPE);
      switch (pMirror) {
         case LEFT_RIGHT:
            switch (railshape) {
               case ASCENDING_NORTH:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
               case ASCENDING_SOUTH:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
               case SOUTH_EAST:
                  return pState.setValue(SHAPE, RailShape.NORTH_EAST);
               case SOUTH_WEST:
                  return pState.setValue(SHAPE, RailShape.NORTH_WEST);
               case NORTH_WEST:
                  return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
               case NORTH_EAST:
                  return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
               default:
                  return super.mirror(pState, pMirror);
            }
         case FRONT_BACK:
            switch (railshape) {
               case ASCENDING_EAST:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);
               case ASCENDING_WEST:
                  return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);
               case ASCENDING_NORTH:
               case ASCENDING_SOUTH:
               default:
                  break;
               case SOUTH_EAST:
                  return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
               case SOUTH_WEST:
                  return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
               case NORTH_WEST:
                  return pState.setValue(SHAPE, RailShape.NORTH_EAST);
               case NORTH_EAST:
                  return pState.setValue(SHAPE, RailShape.NORTH_WEST);
            }
      }

      return super.mirror(pState, pMirror);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(SHAPE, POWERED, WATERLOGGED);
   }
}