package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterSkeleton extends ModelAdapterBiped {
   public ModelAdapterSkeleton() {
      super(EntityType.SKELETON, "skeleton", 0.7F);
   }

   public Model makeModel() {
      return new SkeletonModel(bakeModelLayer(ModelLayers.SKELETON));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      SkeletonRenderer skeletonrenderer = new SkeletonRenderer(entityrenderdispatcher.getContext());
      skeletonrenderer.model = (SkeletonModel)modelBase;
      skeletonrenderer.shadowRadius = shadowSize;
      return skeletonrenderer;
   }
}