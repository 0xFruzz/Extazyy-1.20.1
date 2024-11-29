package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.StrayRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterStray extends ModelAdapterBiped {
   public ModelAdapterStray() {
      super(EntityType.STRAY, "stray", 0.7F);
   }

   public ModelAdapterStray(EntityType type, String name, float shadowSize) {
      super(type, name, shadowSize);
   }

   public Model makeModel() {
      return new SkeletonModel(bakeModelLayer(ModelLayers.STRAY));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      StrayRenderer strayrenderer = new StrayRenderer(entityrenderdispatcher.getContext());
      strayrenderer.model = (SkeletonModel)modelBase;
      strayrenderer.shadowRadius = shadowSize;
      return strayrenderer;
   }
}