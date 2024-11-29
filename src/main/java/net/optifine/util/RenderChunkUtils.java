package net.optifine.util;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class RenderChunkUtils {
   public static int getCountBlocks(ChunkRenderDispatcher.RenderChunk renderChunk) {
      LevelChunkSection[] alevelchunksection = renderChunk.getChunk().getSections();
      if (alevelchunksection == null) {
         return 0;
      } else {
         int i = renderChunk.getOrigin().getY() - renderChunk.getWorld().getMinBuildHeight() >> 4;
         LevelChunkSection levelchunksection = alevelchunksection[i];
         return levelchunksection == null ? 0 : levelchunksection.getBlockRefCount();
      }
   }

   public static double getRelativeBufferSize(ChunkRenderDispatcher.RenderChunk renderChunk) {
      int i = getCountBlocks(renderChunk);
      return getRelativeBufferSize(i);
   }

   public static double getRelativeBufferSize(int blockCount) {
      double d0 = (double)blockCount / 4096.0D;
      d0 *= 0.995D;
      double d1 = d0 * 2.0D - 1.0D;
      d1 = Mth.clamp(d1, -1.0D, 1.0D);
      return Math.sqrt(1.0D - d1 * d1);
   }
}