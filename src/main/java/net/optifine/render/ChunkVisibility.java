package net.optifine.render;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.entity.EntitySection;
import net.optifine.Config;

public class ChunkVisibility {
   public static final int MASK_FACINGS = 63;
   public static final Direction[][] enumFacingArrays = makeEnumFacingArrays(false);
   public static final Direction[][] enumFacingOppositeArrays = makeEnumFacingArrays(true);
   private static int counter = 0;
   private static int iMaxStatic = -1;
   private static int iMaxStaticFinal = 16;
   private static ClientLevel worldLast = null;
   private static int pcxLast = Integer.MIN_VALUE;
   private static int pczLast = Integer.MIN_VALUE;

   public static int getMaxChunkY(ClientLevel world, Entity viewEntity, int renderDistanceChunks, ViewArea viewFrustum) {
      int i = world.getMinBuildHeight();
      int j = world.getHeight();
      int k = i >> 4;
      int l = Mth.floor(viewEntity.getX()) >> 4;
      int i1 = Mth.floor(viewEntity.getY() - (double)i) >> 4;
      int j1 = Mth.floor(viewEntity.getZ()) >> 4;
      int k1 = j - i >> 4;
      i1 = Config.limit(i1, 0, k1 - 1);
      long l1 = SectionPos.asLong(viewEntity.blockPosition());
      EntitySection entitysection = world.getSectionStorage().getSection(l1);
      boolean flag = !Config.isIntegratedServerRunning();
      int i2 = l - renderDistanceChunks;
      int j2 = l + renderDistanceChunks;
      int k2 = j1 - renderDistanceChunks;
      int l2 = j1 + renderDistanceChunks;
      if (world != worldLast || l != pcxLast || j1 != pczLast) {
         counter = 0;
         iMaxStaticFinal = 16;
         worldLast = world;
         pcxLast = l;
         pczLast = j1;
      }

      if (counter == 0) {
         iMaxStatic = -1;
      }

      int i3 = iMaxStatic;
      switch (counter) {
         case 0:
            j2 = l;
            l2 = j1;
            break;
         case 1:
            i2 = l;
            l2 = j1;
            break;
         case 2:
            j2 = l;
            k2 = j1;
            break;
         case 3:
            i2 = l;
            k2 = j1;
      }

      for(int j3 = i2; j3 < j2; ++j3) {
         for(int k3 = k2; k3 < l2; ++k3) {
            LevelChunk levelchunk = world.getChunk(j3, k3);
            if (levelchunk.isEmpty()) {
               if (flag) {
                  int k4 = viewFrustum.getHighestUsedChunkIndex(j3, i3, k3);
                  if (k4 > i3) {
                     i3 = k4;
                  }
               }
            } else {
               LevelChunkSection[] alevelchunksection = levelchunk.getSections();

               for(int l3 = alevelchunksection.length - 1; l3 > i3; --l3) {
                  LevelChunkSection levelchunksection = alevelchunksection[l3];
                  if (levelchunksection != null && !levelchunksection.hasOnlyAir()) {
                     if (l3 > i3) {
                        i3 = l3;
                     }
                     break;
                  }
               }

               try {
                  Map<BlockPos, BlockEntity> map = levelchunk.getBlockEntities();
                  if (!map.isEmpty()) {
                     for(BlockPos blockpos : map.keySet()) {
                        int i4 = blockpos.getY() - i >> 4;
                        if (i4 > i3) {
                           i3 = i4;
                        }
                     }
                  }
               } catch (ConcurrentModificationException concurrentmodificationexception) {
               }
            }
         }
      }

      if (counter == 0) {
         LongSet longset = world.getSectionStorage().getSectionKeys();
         LongIterator longiterator = longset.iterator();

         while(longiterator.hasNext()) {
            long j4 = longiterator.nextLong();
            int l4 = SectionPos.y(j4);
            int i5 = l4 - k;
            if ((j4 != l1 || i5 != i1 || entitysection == null || entitysection.getEntityList().size() != 1) && i5 > i3) {
               i3 = i5;
            }
         }
      }

      if (counter < 3) {
         iMaxStatic = i3;
         i3 = iMaxStaticFinal;
      } else {
         iMaxStaticFinal = i3;
         iMaxStatic = -1;
      }

      counter = (counter + 1) % 4;
      return (i3 << 4) + i;
   }

   public static boolean isFinished() {
      return counter == 0;
   }

   private static Direction[][] makeEnumFacingArrays(boolean opposite) {
      int i = 64;
      Direction[][] adirection = new Direction[i][];

      for(int j = 0; j < i; ++j) {
         List<Direction> list = new ArrayList<>();

         for(int k = 0; k < Direction.VALUES.length; ++k) {
            Direction direction = Direction.VALUES[k];
            Direction direction1 = opposite ? direction.getOpposite() : direction;
            int l = 1 << direction1.ordinal();
            if ((j & l) != 0) {
               list.add(direction);
            }
         }

         Direction[] adirection1 = list.toArray(new Direction[list.size()]);
         adirection[j] = adirection1;
      }

      return adirection;
   }

   public static Direction[] getFacingsNotOpposite(int setDisabled) {
      int i = ~setDisabled & 63;
      return enumFacingOppositeArrays[i];
   }

   public static Direction[] getFacings(int setDirections) {
      int i = setDirections & 63;
      return enumFacingArrays[i];
   }

   public static void reset() {
      worldLast = null;
   }
}