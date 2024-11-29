package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterZombie extends ModelAdapterBiped {
   public ModelAdapterZombie() {
      super(EntityType.ZOMBIE, "zombie", 0.5F);
   }

   protected ModelAdapterZombie(EntityType type, String name, float shadowSize) {
      super(type, name, shadowSize);
   }

   public Model makeModel() {
      return new ZombieModel(bakeModelLayer(ModelLayers.ZOMBIE));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      ZombieRenderer zombierenderer = new ZombieRenderer(entityrenderdispatcher.getContext());
      zombierenderer.model = (ZombieModel)modelBase;
      zombierenderer.shadowRadius = shadowSize;
      return zombierenderer;
   }
}