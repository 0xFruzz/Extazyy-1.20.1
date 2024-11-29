package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.layers.WolfCollarLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterWolfCollar extends ModelAdapterWolf {
   public ModelAdapterWolfCollar() {
      super(EntityType.WOLF, "wolf_collar", 0.5F);
   }

   public Model makeModel() {
      return new WolfModel(bakeModelLayer(ModelLayers.WOLF));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      WolfRenderer wolfrenderer = new WolfRenderer(entityrenderdispatcher.getContext());
      wolfrenderer.model = new WolfModel<>(bakeModelLayer(ModelLayers.WOLF));
      wolfrenderer.shadowRadius = 0.5F;
      EntityRenderer entityrenderer = rendererCache.get(EntityType.WOLF, index, () -> {
         return wolfrenderer;
      });
      if (!(entityrenderer instanceof WolfRenderer wolfrenderer1)) {
         Config.warn("Not a RenderWolf: " + entityrenderer);
         return null;
      } else {
         WolfCollarLayer wolfcollarlayer = new WolfCollarLayer(wolfrenderer1);
         wolfcollarlayer.model = (WolfModel)modelBase;
         wolfrenderer1.removeLayers(WolfCollarLayer.class);
         wolfrenderer1.addLayer(wolfcollarlayer);
         return wolfrenderer1;
      }
   }

   public boolean setTextureLocation(IEntityRenderer er, ResourceLocation textureLocation) {
      WolfRenderer wolfrenderer = (WolfRenderer)er;

      for(WolfCollarLayer wolfcollarlayer : wolfrenderer.getLayers(WolfCollarLayer.class)) {
         wolfcollarlayer.model.locationTextureCustom = textureLocation;
      }

      return true;
   }
}