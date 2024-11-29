package net.minecraftforge.client.model.data;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ModelDataManager {
   public ModelDataManager(Level clientLevel) {
   }

   public ModelData getAt(BlockPos blockPos) {
      return null;
   }

   public Map<BlockPos, ModelData> getAt(ChunkPos pos) {
      return null;
   }
}