package net.optifine;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class DynamicLight {
   private Entity entity = null;
   private double offsetY = 0.0D;
   private double lastPosX = (double)Integer.MIN_VALUE;
   private double lastPosY = (double)Integer.MIN_VALUE;
   private double lastPosZ = (double)Integer.MIN_VALUE;
   private int lastLightLevel = 0;
   private long timeCheckMs = 0L;
   private Set<BlockPos> setLitChunkPos = new HashSet<>();
   private BlockPos.MutableBlockPos blockPosMutable = new BlockPos.MutableBlockPos();

   public DynamicLight(Entity entity) {
      this.entity = entity;
      this.offsetY = (double)entity.getEyeHeight();
   }

   public void update(LevelRenderer renderGlobal) {
      if (Config.isDynamicLightsFast()) {
         long i = System.currentTimeMillis();
         if (i < this.timeCheckMs + 500L) {
            return;
         }

         this.timeCheckMs = i;
      }

      double d6 = this.entity.getX() - 0.5D;
      double d0 = this.entity.getY() - 0.5D + this.offsetY;
      double d1 = this.entity.getZ() - 0.5D;
      int j = DynamicLights.getLightLevel(this.entity);
      double d2 = d6 - this.lastPosX;
      double d3 = d0 - this.lastPosY;
      double d4 = d1 - this.lastPosZ;
      double d5 = 0.1D;
      if (!(Math.abs(d2) <= d5) || !(Math.abs(d3) <= d5) || !(Math.abs(d4) <= d5) || this.lastLightLevel != j) {
         this.lastPosX = d6;
         this.lastPosY = d0;
         this.lastPosZ = d1;
         this.lastLightLevel = j;
         Set<BlockPos> set = new HashSet<>();
         if (j > 0) {
            Direction direction = (Mth.floor(d6) & 15) >= 8 ? Direction.EAST : Direction.WEST;
            Direction direction1 = (Mth.floor(d0) & 15) >= 8 ? Direction.UP : Direction.DOWN;
            Direction direction2 = (Mth.floor(d1) & 15) >= 8 ? Direction.SOUTH : Direction.NORTH;
            BlockPos blockpos = BlockPos.containing(d6, d0, d1);
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = renderGlobal.getRenderChunk(blockpos);
            BlockPos blockpos1 = this.getChunkPos(chunkrenderdispatcher$renderchunk, blockpos, direction);
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk1 = renderGlobal.getRenderChunk(blockpos1);
            BlockPos blockpos2 = this.getChunkPos(chunkrenderdispatcher$renderchunk, blockpos, direction2);
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk2 = renderGlobal.getRenderChunk(blockpos2);
            BlockPos blockpos3 = this.getChunkPos(chunkrenderdispatcher$renderchunk1, blockpos1, direction2);
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk3 = renderGlobal.getRenderChunk(blockpos3);
            BlockPos blockpos4 = this.getChunkPos(chunkrenderdispatcher$renderchunk, blockpos, direction1);
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk4 = renderGlobal.getRenderChunk(blockpos4);
            BlockPos blockpos5 = this.getChunkPos(chunkrenderdispatcher$renderchunk4, blockpos4, direction);
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk5 = renderGlobal.getRenderChunk(blockpos5);
            BlockPos blockpos6 = this.getChunkPos(chunkrenderdispatcher$renderchunk4, blockpos4, direction2);
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk6 = renderGlobal.getRenderChunk(blockpos6);
            BlockPos blockpos7 = this.getChunkPos(chunkrenderdispatcher$renderchunk5, blockpos5, direction2);
            ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk7 = renderGlobal.getRenderChunk(blockpos7);
            this.updateChunkLight(chunkrenderdispatcher$renderchunk, this.setLitChunkPos, set);
            this.updateChunkLight(chunkrenderdispatcher$renderchunk1, this.setLitChunkPos, set);
            this.updateChunkLight(chunkrenderdispatcher$renderchunk2, this.setLitChunkPos, set);
            this.updateChunkLight(chunkrenderdispatcher$renderchunk3, this.setLitChunkPos, set);
            this.updateChunkLight(chunkrenderdispatcher$renderchunk4, this.setLitChunkPos, set);
            this.updateChunkLight(chunkrenderdispatcher$renderchunk5, this.setLitChunkPos, set);
            this.updateChunkLight(chunkrenderdispatcher$renderchunk6, this.setLitChunkPos, set);
            this.updateChunkLight(chunkrenderdispatcher$renderchunk7, this.setLitChunkPos, set);
         }

         this.updateLitChunks(renderGlobal);
         this.setLitChunkPos = set;
      }
   }

   private BlockPos getChunkPos(ChunkRenderDispatcher.RenderChunk renderChunk, BlockPos pos, Direction facing) {
      return renderChunk != null ? renderChunk.getRelativeOrigin(facing) : pos.relative(facing, 16);
   }

   private void updateChunkLight(ChunkRenderDispatcher.RenderChunk renderChunk, Set<BlockPos> setPrevPos, Set<BlockPos> setNewPos) {
      if (renderChunk != null) {
         ChunkRenderDispatcher.CompiledChunk chunkrenderdispatcher$compiledchunk = renderChunk.getCompiledChunk();
         if (chunkrenderdispatcher$compiledchunk != null && !chunkrenderdispatcher$compiledchunk.hasNoRenderableLayers()) {
            renderChunk.setDirty(false);
            renderChunk.setNeedsBackgroundPriorityUpdate(true);
         }

         BlockPos blockpos = renderChunk.getOrigin().immutable();
         if (setPrevPos != null) {
            setPrevPos.remove(blockpos);
         }

         if (setNewPos != null) {
            setNewPos.add(blockpos);
         }

      }
   }

   public void updateLitChunks(LevelRenderer renderGlobal) {
      for(BlockPos blockpos : this.setLitChunkPos) {
         ChunkRenderDispatcher.RenderChunk chunkrenderdispatcher$renderchunk = renderGlobal.getRenderChunk(blockpos);
         this.updateChunkLight(chunkrenderdispatcher$renderchunk, (Set<BlockPos>)null, (Set<BlockPos>)null);
      }

   }

   public Entity getEntity() {
      return this.entity;
   }

   public double getLastPosX() {
      return this.lastPosX;
   }

   public double getLastPosY() {
      return this.lastPosY;
   }

   public double getLastPosZ() {
      return this.lastPosZ;
   }

   public int getLastLightLevel() {
      return this.lastLightLevel;
   }

   public double getOffsetY() {
      return this.offsetY;
   }

   public String toString() {
      return "Entity: " + this.entity + ", offsetY: " + this.offsetY;
   }
}