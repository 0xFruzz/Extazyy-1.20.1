package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterCreeperCharge extends ModelAdapterCreeper {
   public ModelAdapterCreeperCharge() {
      super(EntityType.CREEPER, "creeper_charge", 0.25F);
   }

   public Model makeModel() {
      return new CreeperModel(bakeModelLayer(ModelLayers.CREEPER_ARMOR));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      CreeperRenderer creeperrenderer = new CreeperRenderer(entityrenderdispatcher.getContext());
      creeperrenderer.model = new CreeperModel<>(bakeModelLayer(ModelLayers.CREEPER_ARMOR));
      creeperrenderer.shadowRadius = 0.25F;
      EntityRenderer entityrenderer = rendererCache.get(EntityType.CREEPER, index, () -> {
         return creeperrenderer;
      });
      if (!(entityrenderer instanceof CreeperRenderer creeperrenderer1)) {
         Config.warn("Not a CreeperRenderer: " + entityrenderer);
         return null;
      } else {
         CreeperPowerLayer creeperpowerlayer = new CreeperPowerLayer(creeperrenderer1, entityrenderdispatcher.getContext().getModelSet());
         creeperpowerlayer.model = (CreeperModel)modelBase;
         creeperrenderer1.removeLayers(CreeperPowerLayer.class);
         creeperrenderer1.addLayer(creeperpowerlayer);
         return creeperrenderer1;
      }
   }

   public boolean setTextureLocation(IEntityRenderer er, ResourceLocation textureLocation) {
      CreeperRenderer creeperrenderer = (CreeperRenderer)er;

      for(CreeperPowerLayer creeperpowerlayer : creeperrenderer.getLayers(CreeperPowerLayer.class)) {
         creeperpowerlayer.customTextureLocation = textureLocation;
      }

      return true;
   }
}