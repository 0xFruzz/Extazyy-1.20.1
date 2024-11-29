package net.optifine.render;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.override.ChunkCacheOF;

public class LightCacheOF {
   public static final float getBrightness(BlockState blockStateIn, BlockAndTintGetter worldIn, BlockPos blockPosIn) {
      float f = blockStateIn.getShadeBrightness(worldIn, blockPosIn);
      return ModelBlockRenderer.fixAoLightValue(f);
   }

   public static final int getPackedLight(BlockState blockStateIn, BlockAndTintGetter worldIn, BlockPos blockPosIn) {
      if (worldIn instanceof ChunkCacheOF chunkcacheof) {
         int[] aint = chunkcacheof.getCombinedLights();
         int i = chunkcacheof.getPositionIndex(blockPosIn);
         if (i >= 0 && i < aint.length && aint != null) {
            int j = aint[i];
            if (j == -1) {
               j = LevelRenderer.getLightColor(worldIn, blockStateIn, blockPosIn);
               aint[i] = j;
            }

            return j;
         } else {
            return LevelRenderer.getLightColor(worldIn, blockStateIn, blockPosIn);
         }
      } else {
         return LevelRenderer.getLightColor(worldIn, blockStateIn, blockPosIn);
      }
   }
}