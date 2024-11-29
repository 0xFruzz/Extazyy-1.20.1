package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterCatCollar extends ModelAdapterOcelot {
   public ModelAdapterCatCollar() {
      super(EntityType.CAT, "cat_collar", 0.4F);
   }

   public Model makeModel() {
      return new CatModel(bakeModelLayer(ModelLayers.CAT_COLLAR));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      CatRenderer catrenderer = new CatRenderer(entityrenderdispatcher.getContext());
      catrenderer.model = new CatModel<>(bakeModelLayer(ModelLayers.CAT_COLLAR));
      catrenderer.shadowRadius = 0.4F;
      EntityRenderer entityrenderer = rendererCache.get(EntityType.CAT, index, () -> {
         return catrenderer;
      });
      if (!(entityrenderer instanceof CatRenderer catrenderer1)) {
         Config.warn("Not a RenderCat: " + entityrenderer);
         return null;
      } else {
         CatCollarLayer catcollarlayer = new CatCollarLayer(catrenderer1, entityrenderdispatcher.getContext().getModelSet());
         catcollarlayer.catModel = (CatModel)modelBase;
         catrenderer1.removeLayers(CatCollarLayer.class);
         catrenderer1.addLayer(catcollarlayer);
         return catrenderer1;
      }
   }

   public boolean setTextureLocation(IEntityRenderer er, ResourceLocation textureLocation) {
      CatRenderer catrenderer = (CatRenderer)er;

      for(CatCollarLayer catcollarlayer : catrenderer.getLayers(CatCollarLayer.class)) {
         catcollarlayer.catModel.locationTextureCustom = textureLocation;
      }

      return true;
   }
}