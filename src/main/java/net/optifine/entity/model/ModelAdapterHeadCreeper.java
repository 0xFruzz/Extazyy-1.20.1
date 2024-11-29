package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.level.block.SkullBlock;

public class ModelAdapterHeadCreeper extends ModelAdapterHead {
   public ModelAdapterHeadCreeper() {
      super("head_creeper", ModelLayers.CREEPER_HEAD, SkullBlock.Types.CREEPER);
   }
}