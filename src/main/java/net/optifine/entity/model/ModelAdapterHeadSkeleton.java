package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.level.block.SkullBlock;

public class ModelAdapterHeadSkeleton extends ModelAdapterHead {
   public ModelAdapterHeadSkeleton() {
      super("head_skeleton", ModelLayers.SKELETON_SKULL, SkullBlock.Types.SKELETON);
   }
}