package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.layers.SheepFurLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterSheepWool extends ModelAdapterQuadruped {
   public ModelAdapterSheepWool() {
      super(EntityType.SHEEP, "sheep_wool", 0.7F);
   }

   public Model makeModel() {
      return new SheepFurModel(bakeModelLayer(ModelLayers.SHEEP_FUR));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      SheepRenderer sheeprenderer = new SheepRenderer(entityrenderdispatcher.getContext());
      sheeprenderer.model = new SheepModel<>(bakeModelLayer(ModelLayers.SHEEP_FUR));
      sheeprenderer.shadowRadius = 0.7F;
      EntityRenderer entityrenderer = rendererCache.get(EntityType.SHEEP, index, () -> {
         return sheeprenderer;
      });
      if (!(entityrenderer instanceof SheepRenderer sheeprenderer1)) {
         Config.warn("Not a RenderSheep: " + entityrenderer);
         return null;
      } else {
         SheepFurLayer sheepfurlayer = new SheepFurLayer(sheeprenderer1, entityrenderdispatcher.getContext().getModelSet());
         sheepfurlayer.model = (SheepFurModel)modelBase;
         sheeprenderer1.removeLayers(SheepFurLayer.class);
         sheeprenderer1.addLayer(sheepfurlayer);
         return sheeprenderer1;
      }
   }

   public boolean setTextureLocation(IEntityRenderer er, ResourceLocation textureLocation) {
      SheepRenderer sheeprenderer = (SheepRenderer)er;

      for(SheepFurLayer sheepfurlayer : sheeprenderer.getLayers(SheepFurLayer.class)) {
         sheepfurlayer.model.locationTextureCustom = textureLocation;
      }

      return true;
   }
}