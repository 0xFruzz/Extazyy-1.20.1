package net.optifine;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class ChunkOF extends LevelChunk {
   private boolean hasEntitiesOF;
   private boolean loadedOF;

   public ChunkOF(Level worldIn, ChunkPos chunkPosIn) {
      super(worldIn, chunkPosIn);
   }

   public void addEntity(Entity entityIn) {
      this.hasEntitiesOF = true;
      super.addEntity(entityIn);
   }

   public boolean hasEntities() {
      return this.hasEntitiesOF;
   }

   public void setLoaded(boolean loaded) {
      this.loadedOF = loaded;
      super.setLoaded(loaded);
   }

   public boolean isLoaded() {
      return this.loadedOF;
   }
}