package net.optifine.util;

import net.minecraft.world.level.chunk.LevelChunk;
import net.optifine.ChunkOF;

public class ChunkUtils {
   public static boolean hasEntities(LevelChunk chunk) {
      if (chunk instanceof ChunkOF chunkof) {
         return chunkof.hasEntities();
      } else {
         return true;
      }
   }

   public static boolean isLoaded(LevelChunk chunk) {
      if (chunk instanceof ChunkOF chunkof) {
         return chunkof.isLoaded();
      } else {
         return false;
      }
   }
}