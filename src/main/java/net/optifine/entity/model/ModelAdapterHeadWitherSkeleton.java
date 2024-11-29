package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.level.block.SkullBlock;

public class ModelAdapterHeadWitherSkeleton extends ModelAdapterHead {
   public ModelAdapterHeadWitherSkeleton() {
      super("head_wither_skeleton", ModelLayers.WITHER_SKELETON_SKULL, SkullBlock.Types.WITHER_SKELETON);
   }
}