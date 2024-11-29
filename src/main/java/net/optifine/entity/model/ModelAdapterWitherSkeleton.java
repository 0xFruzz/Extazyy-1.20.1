package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.WitherSkeletonRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterWitherSkeleton extends ModelAdapterBiped {
   public ModelAdapterWitherSkeleton() {
      super(EntityType.WITHER_SKELETON, "wither_skeleton", 0.7F);
   }

   public Model makeModel() {
      return new SkeletonModel(bakeModelLayer(ModelLayers.WITHER_SKELETON));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      WitherSkeletonRenderer witherskeletonrenderer = new WitherSkeletonRenderer(entityrenderdispatcher.getContext());
      witherskeletonrenderer.model = (SkeletonModel)modelBase;
      witherskeletonrenderer.shadowRadius = shadowSize;
      return witherskeletonrenderer;
   }
}