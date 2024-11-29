package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterEvoker extends ModelAdapterIllager {
   public ModelAdapterEvoker() {
      super(EntityType.EVOKER, "evoker", 0.5F, new String[]{"evocation_illager"});
   }

   public Model makeModel() {
      return new IllagerModel(bakeModelLayer(ModelLayers.EVOKER));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      EvokerRenderer evokerrenderer = new EvokerRenderer(entityrenderdispatcher.getContext());
      evokerrenderer.model = (EntityModel)modelBase;
      evokerrenderer.shadowRadius = shadowSize;
      return evokerrenderer;
   }
}