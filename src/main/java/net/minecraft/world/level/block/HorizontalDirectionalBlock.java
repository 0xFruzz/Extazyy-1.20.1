package net.minecraft.world.level.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public abstract class HorizontalDirectionalBlock extends Block {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

   protected HorizontalDirectionalBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public BlockState rotate(BlockState pState, Rotation pRot) {
      return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
   }

   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
   }
}