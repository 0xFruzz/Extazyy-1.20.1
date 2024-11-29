package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterEnderman extends ModelAdapterBiped {
   public ModelAdapterEnderman() {
      super(EntityType.ENDERMAN, "enderman", 0.5F);
   }

   public Model makeModel() {
      return new EndermanModel(bakeModelLayer(ModelLayers.ENDERMAN));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      EndermanRenderer endermanrenderer = new EndermanRenderer(entityrenderdispatcher.getContext());
      endermanrenderer.model = (EndermanModel)modelBase;
      endermanrenderer.shadowRadius = shadowSize;
      return endermanrenderer;
   }
}