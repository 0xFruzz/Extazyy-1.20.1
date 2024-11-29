package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.StrayRenderer;
import net.minecraft.client.renderer.entity.layers.StrayClothingLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterStrayOuter extends ModelAdapterStray {
   public ModelAdapterStrayOuter() {
      super(EntityType.STRAY, "stray_outer", 0.7F);
   }

   public Model makeModel() {
      return new SkeletonModel(bakeModelLayer(ModelLayers.STRAY_OUTER_LAYER));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      StrayRenderer strayrenderer = new StrayRenderer(entityrenderdispatcher.getContext());
      strayrenderer.model = new SkeletonModel<>(bakeModelLayer(ModelLayers.STRAY_OUTER_LAYER));
      strayrenderer.shadowRadius = 0.7F;
      EntityRenderer entityrenderer = rendererCache.get(EntityType.STRAY, index, () -> {
         return strayrenderer;
      });
      if (!(entityrenderer instanceof StrayRenderer strayrenderer1)) {
         Config.warn("Not a SkeletonModelRenderer: " + entityrenderer);
         return null;
      } else {
         StrayClothingLayer strayclothinglayer = new StrayClothingLayer<>(strayrenderer1, entityrenderdispatcher.getContext().getModelSet());
         strayclothinglayer.layerModel = (SkeletonModel)modelBase;
         strayrenderer1.removeLayers(StrayClothingLayer.class);
         strayrenderer1.addLayer(strayclothinglayer);
         return strayrenderer1;
      }
   }

   public boolean setTextureLocation(IEntityRenderer er, ResourceLocation textureLocation) {
      StrayRenderer strayrenderer = (StrayRenderer)er;

      for(StrayClothingLayer strayclothinglayer : strayrenderer.getLayers(StrayClothingLayer.class)) {
         strayclothinglayer.customTextureLocation = textureLocation;
      }

      return true;
   }
}