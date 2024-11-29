package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterDrowned extends ModelAdapterZombie {
   public ModelAdapterDrowned() {
      super(EntityType.DROWNED, "drowned", 0.5F);
   }

   public ModelAdapterDrowned(EntityType type, String name, float shadowSize) {
      super(type, name, shadowSize);
   }

   public Model makeModel() {
      return new DrownedModel(bakeModelLayer(ModelLayers.DROWNED));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      DrownedRenderer drownedrenderer = new DrownedRenderer(entityrenderdispatcher.getContext());
      drownedrenderer.model = (DrownedModel)modelBase;
      drownedrenderer.shadowRadius = shadowSize;
      return drownedrenderer;
   }
}