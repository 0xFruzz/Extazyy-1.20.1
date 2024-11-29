package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.level.block.SkullBlock;

public class ModelAdapterHeadZombie extends ModelAdapterHead {
   public ModelAdapterHeadZombie() {
      super("head_zombie", ModelLayers.ZOMBIE_HEAD, SkullBlock.Types.ZOMBIE);
   }
}