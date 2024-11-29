package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperFullBlock extends Block implements WeatheringCopper {
   private final WeatheringCopper.WeatherState weatherState;

   public WeatheringCopperFullBlock(WeatheringCopper.WeatherState pWeatherState, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.weatherState = pWeatherState;
   }

   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      this.onRandomTick(pState, pLevel, pPos, pRandom);
   }

   public boolean isRandomlyTicking(BlockState pState) {
      return WeatheringCopper.getNext(pState.getBlock()).isPresent();
   }

   public WeatheringCopper.WeatherState getAge() {
      return this.weatherState;
   }
}