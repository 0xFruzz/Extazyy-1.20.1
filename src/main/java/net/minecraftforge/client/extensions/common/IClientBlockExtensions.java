package net.minecraftforge.client.extensions.common;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public interface IClientBlockExtensions {
   IClientBlockExtensions DUMMY = new IClientBlockExtensions() {
   };

   static IClientBlockExtensions of(BlockState blockState) {
      return DUMMY;
   }

   default boolean addDestroyEffects(BlockState state, Level Level, BlockPos pos, ParticleEngine manager) {
      return false;
   }

   default boolean addHitEffects(BlockState state, Level Level, HitResult target, ParticleEngine manager) {
      return false;
   }
}