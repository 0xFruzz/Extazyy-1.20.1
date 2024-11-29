package net.minecraft.client.renderer;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.optifine.Config;
import net.optifine.render.ClearVertexBuffersTask;
import net.optifine.render.VboRegion;

public class ViewArea {
   protected final LevelRenderer levelRenderer;
   protected final Level level;
   protected int chunkGridSizeY;
   protected int chunkGridSizeX;
   protected int chunkGridSizeZ;
   public ChunkRenderDispatcher.RenderChunk[] chunks;
   private Map<ChunkPos, VboRegion[]> mapVboRegions = new HashMap<>();
   private int lastCleanIndex = 0;

   public ViewArea(ChunkRenderDispatcher pChunkRenderDispatcher, Level pLevel, int pViewDistance, LevelRenderer pLevelRenderer) {
      this.levelRenderer = pLevelRenderer;
      this.level = pLevel;
      this.setViewDistance(pViewDistance);
      this.createChunks(pChunkRenderDispatcher);
   }

   protected void createChunks(ChunkRenderDispatcher pRenderChunkFactory) {
      if (!Minecraft.getInstance().isSameThread()) {
         throw new IllegalStateException("createChunks called from wrong thread: " + Thread.currentThread().getName());
      } else {
         int i = this.chunkGridSizeX * this.chunkGridSizeY * this.chunkGridSizeZ;
         this.chunks = new ChunkRenderDispatcher.RenderChunk[i];
         int j = this.level.getMinBuildHeight();

         for(int k = 0; k < this.chunkGridSizeX; ++k) {
            for(int l = 0; l < this.chunkGridSizeY; ++l) {
               for(int i1 = 0; i1 < this.chunkGridSizeZ; ++i1) {
                  int j1 = this.getChunkIndex(k, l, i1);
                  this.chunks[j1] = pRenderChunkFactory.new RenderChunk(j1, k * 16, l * 16, i1 * 16);
                  this.chunks[j1].setOrigin(k * 16, l * 16 + j, i1 * 16);
                  if (Config.isVbo() && Config.isRenderRegions()) {
                     this.updateVboRegion(this.chunks[j1]);
                  }
               }
            }
         }

         for(int k1 = 0; k1 < this.chunks.length; ++k1) {
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk1 = this.chunks[k1];

            for(int l1 = 0; l1 < Direction.VALUES.length; ++l1) {
               Direction direction = Direction.VALUES[l1];
               BlockPos blockpos = chunkrenderdispatcher$renderchunk1.getRelativeOrigin(direction);
               ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.getRenderChunkAt(blockpos);
               chunkrenderdispatcher$renderchunk1.setRenderChunkNeighbour(direction, chunkrenderdispatcher$renderchunk);
            }
         }

      }
   }

   public void releaseAllBuffers() {
      for(ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk : this.chunks) {
         chunkrenderdispatcher$renderchunk.releaseBuffers();
      }

      this.deleteVboRegions();
   }

   private int getChunkIndex(int pX, int pY, int pZ) {
      return (pZ * this.chunkGridSizeY + pY) * this.chunkGridSizeX + pX;
   }

   protected void setViewDistance(int pRenderDistanceChunks) {
      int i = pRenderDistanceChunks * 2 + 1;
      this.chunkGridSizeX = i;
      this.chunkGridSizeY = this.level.getSectionsCount();
      this.chunkGridSizeZ = i;
   }

   public void repositionCamera(double pViewEntityX, double pViewEntityZ) {
      int i = Mth.ceil(pViewEntityX);
      int j = Mth.ceil(pViewEntityZ);

      for(int k = 0; k < this.chunkGridSizeX; ++k) {
         int l = this.chunkGridSizeX * 16;
         int i1 = i - 7 - l / 2;
         int j1 = i1 + Math.floorMod(k * 16 - i1, l);

         for(int k1 = 0; k1 < this.chunkGridSizeZ; ++k1) {
            int l1 = this.chunkGridSizeZ * 16;
            int i2 = j - 7 - l1 / 2;
            int j2 = i2 + Math.floorMod(k1 * 16 - i2, l1);

            for(int k2 = 0; k2 < this.chunkGridSizeY; ++k2) {
               int l2 = this.level.getMinBuildHeight() + k2 * 16;
               ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.chunks[this.getChunkIndex(k, k2, k1)];
               BlockPos blockpos = chunkrenderdispatcher$renderchunk.getOrigin();
               if (j1 != blockpos.getX() || l2 != blockpos.getY() || j2 != blockpos.getZ()) {
                  chunkrenderdispatcher$renderchunk.setOrigin(j1, l2, j2);
               }
            }
         }
      }

   }

   public void setDirty(int pSectionX, int pSectionY, int pSectionZ, boolean pReRenderOnMainThread) {
      int i = Math.floorMod(pSectionX, this.chunkGridSizeX);
      int j = Math.floorMod(pSectionY - this.level.getMinSection(), this.chunkGridSizeY);
      int k = Math.floorMod(pSectionZ, this.chunkGridSizeZ);
      ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.chunks[this.getChunkIndex(i, j, k)];
      if (!chunkrenderdispatcher$renderchunk.isDirty()) {
         this.levelRenderer.addRecentlyCompiledChunk(chunkrenderdispatcher$renderchunk);
      }

      chunkrenderdispatcher$renderchunk.setDirty(pReRenderOnMainThread);
   }

   @Nullable
   public ChunkRenderDispatcher.RenderChunk getRenderChunkAt(BlockPos pPos) {
      int i = pPos.getX() >> 4;
      int j = pPos.getY() - this.level.getMinBuildHeight() >> 4;
      int k = pPos.getZ() >> 4;
      if (j >= 0 && j < this.chunkGridSizeY) {
         i = Mth.positiveModulo(i, this.chunkGridSizeX);
         k = Mth.positiveModulo(k, this.chunkGridSizeZ);
         return this.chunks[this.getChunkIndex(i, j, k)];
      } else {
         return null;
      }
   }

   private void updateVboRegion(ChunkRenderDispatcher.RenderChunk renderChunk) {
      BlockPos blockpos = renderChunk.getOrigin();
      int i = blockpos.getX() >> 8 << 8;
      int j = blockpos.getZ() >> 8 << 8;
      ChunkPos chunkpos = new ChunkPos(i, j);
      RenderType[] arendertype = RenderType.CHUNK_RENDER_TYPES;
      VboRegion[] avboregion = this.mapVboRegions.get(chunkpos);
      if (avboregion == null) {
         avboregion = new VboRegion[arendertype.length];

         for(int k = 0; k < arendertype.length; ++k) {
            if (!arendertype[k].isNeedsSorting()) {
               avboregion[k] = new VboRegion(arendertype[k]);
            }
         }

         this.mapVboRegions.put(chunkpos, avboregion);
      }

      for(int l = 0; l < arendertype.length; ++l) {
         RenderType rendertype = arendertype[l];
         VboRegion vboregion = avboregion[l];
         renderChunk.getBuffer(rendertype).setVboRegion(vboregion);
      }

   }

   public void deleteVboRegions() {
      for(ChunkPos chunkpos : this.mapVboRegions.keySet()) {
         VboRegion[] avboregion = this.mapVboRegions.get(chunkpos);

         for(int i = 0; i < avboregion.length; ++i) {
            VboRegion vboregion = avboregion[i];
            if (vboregion != null) {
               vboregion.deleteGlBuffers();
            }

            avboregion[i] = null;
         }
      }

      this.mapVboRegions.clear();
   }

   public int getHighestUsedChunkIndex(int chunkX, int minChunkIndex, int chunkZ) {
      chunkX = Mth.positiveModulo(chunkX, this.chunkGridSizeX);
      minChunkIndex = Mth.clamp(minChunkIndex, 0, this.chunkGridSizeY);
      chunkZ = Mth.positiveModulo(chunkZ, this.chunkGridSizeZ);

      for(int i = this.chunkGridSizeY - 1; i >= minChunkIndex; --i) {
         ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.chunks[this.getChunkIndex(chunkX, i, chunkZ)];
         if (!chunkrenderdispatcher$renderchunk.getCompiledChunk().hasNoRenderableLayers()) {
            return i;
         }
      }

      return -1;
   }

   public void clearUnusedVbos() {
      int i = Config.limit(Config.getFpsAverage(), 1, 1000);
      int j = Config.limit(this.chunks.length / (10 * i), 3, 100);
      int k = Config.limit(j / 3, 1, 3);
      int l = 0;
      int i1 = Config.limit(this.lastCleanIndex, 0, this.chunks.length - 1);

      for(int j1 = Math.min(i1 + j, this.chunks.length); i1 < j1 && l < k; ++i1) {
         ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = this.chunks[i1];
         ClearVertexBuffersTask clearvertexbufferstask = ClearVertexBuffersTask.make(chunkrenderdispatcher$renderchunk.getCompiledChunk().getLayersUsed(), chunkrenderdispatcher$renderchunk);
         if (clearvertexbufferstask != null) {
            Minecraft.getInstance().levelRenderer.getChunkRenderDispatcher().addUploadTask(clearvertexbufferstask);
            ++l;
         }
      }

      if (i1 >= this.chunks.length) {
         i1 = 0;
      }

      this.lastCleanIndex = i1;
   }
}