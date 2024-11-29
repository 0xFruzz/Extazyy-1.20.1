package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterSheep extends ModelAdapterQuadruped {
   public ModelAdapterSheep() {
      super(EntityType.SHEEP, "sheep", 0.7F);
   }

   public Model makeModel() {
      return new SheepModel(bakeModelLayer(ModelLayers.SHEEP));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      SheepRenderer sheeprenderer = new SheepRenderer(entityrenderdispatcher.getContext());
      sheeprenderer.model = (SheepModel)modelBase;
      sheeprenderer.shadowRadius = shadowSize;
      return sheeprenderer;
   }
}