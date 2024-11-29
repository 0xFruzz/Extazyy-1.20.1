package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterSkeletonHorse extends ModelAdapterHorse {
   public ModelAdapterSkeletonHorse() {
      super(EntityType.SKELETON_HORSE, "skeleton_horse", 0.75F);
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      UndeadHorseRenderer undeadhorserenderer = new UndeadHorseRenderer(entityrenderdispatcher.getContext(), ModelLayers.SKELETON_HORSE);
      undeadhorserenderer.model = (HorseModel)modelBase;
      undeadhorserenderer.shadowRadius = shadowSize;
      return undeadhorserenderer;
   }
}