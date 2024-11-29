package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterZombifiedPiglin extends ModelAdapterPiglin {
   public ModelAdapterZombifiedPiglin() {
      super(EntityType.ZOMBIFIED_PIGLIN, "zombified_piglin", 0.5F);
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      PiglinRenderer piglinrenderer = new PiglinRenderer(entityrenderdispatcher.getContext(), ModelLayers.ZOMBIFIED_PIGLIN, ModelLayers.ZOMBIFIED_PIGLIN_INNER_ARMOR, ModelLayers.ZOMBIFIED_PIGLIN_OUTER_ARMOR, true);
      piglinrenderer.model = (PiglinModel)modelBase;
      piglinrenderer.shadowRadius = shadowSize;
      return piglinrenderer;
   }
}