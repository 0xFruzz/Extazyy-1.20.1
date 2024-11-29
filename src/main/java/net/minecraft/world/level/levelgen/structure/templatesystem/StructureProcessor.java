package net.minecraft.world.level.levelgen.structure.templatesystem;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;

public abstract class StructureProcessor {
   @Nullable
   public StructureTemplate.StructureBlockInfo processBlock(LevelReader pLevel, BlockPos pOffset, BlockPos pPos, StructureTemplate.StructureBlockInfo pBlockInfo, StructureTemplate.StructureBlockInfo pRelativeBlockInfo, StructurePlaceSettings pSettings) {
      return pRelativeBlockInfo;
   }

   protected abstract StructureProcessorType<?> getType();

   public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor pServerLevel, BlockPos pOffset, BlockPos pPos, List<StructureTemplate.StructureBlockInfo> pOriginalBlockInfos, List<StructureTemplate.StructureBlockInfo> pProcessedBlockInfos, StructurePlaceSettings pSettings) {
      return pProcessedBlockInfos;
   }
}