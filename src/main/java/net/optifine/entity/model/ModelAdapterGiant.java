package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.GiantZombieModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.GiantMobRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterGiant extends ModelAdapterZombie {
   public ModelAdapterGiant() {
      super(EntityType.GIANT, "giant", 3.0F);
   }

   public Model makeModel() {
      return new GiantZombieModel(bakeModelLayer(ModelLayers.GIANT));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      GiantMobRenderer giantmobrenderer = new GiantMobRenderer(entityrenderdispatcher.getContext(), 6.0F);
      giantmobrenderer.model = (GiantZombieModel)modelBase;
      giantmobrenderer.shadowRadius = shadowSize;
      return giantmobrenderer;
   }
}