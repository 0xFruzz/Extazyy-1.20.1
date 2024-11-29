package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterCat extends ModelAdapterOcelot {
   public ModelAdapterCat() {
      super(EntityType.CAT, "cat", 0.4F);
   }

   public Model makeModel() {
      return new CatModel(bakeModelLayer(ModelLayers.CAT));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      CatRenderer catrenderer = new CatRenderer(entityrenderdispatcher.getContext());
      catrenderer.model = (CatModel)modelBase;
      catrenderer.shadowRadius = shadowSize;
      return catrenderer;
   }
}