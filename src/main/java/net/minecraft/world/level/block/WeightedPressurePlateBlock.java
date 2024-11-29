package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WeightedPressurePlateBlock extends BasePressurePlateBlock {
   public static final IntegerProperty POWER = BlockStateProperties.POWER;
   private final int maxWeight;

   protected WeightedPressurePlateBlock(int pMaxWeight, BlockBehaviour.Properties pProperties, BlockSetType pType) {
      super(pProperties, pType);
      this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
      this.maxWeight = pMaxWeight;
   }

   protected int getSignalStrength(Level pLevel, BlockPos pPos) {
      int i = Math.min(getEntityCount(pLevel, TOUCH_AABB.move(pPos), Entity.class), this.maxWeight);
      if (i > 0) {
         float f = (float)Math.min(this.maxWeight, i) / (float)this.maxWeight;
         return Mth.ceil(f * 15.0F);
      } else {
         return 0;
      }
   }

   protected int getSignalForState(BlockState pState) {
      return pState.getValue(POWER);
   }

   protected BlockState setSignalForState(BlockState pState, int pStrength) {
      return pState.setValue(POWER, Integer.valueOf(pStrength));
   }

   protected int getPressedTime() {
      return 10;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(POWER);
   }
}