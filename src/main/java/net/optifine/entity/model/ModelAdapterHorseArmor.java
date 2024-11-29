package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterHorseArmor extends ModelAdapterHorse {
   public ModelAdapterHorseArmor() {
      super(EntityType.HORSE, "horse_armor", 0.75F);
   }

   public Model makeModel() {
      return new HorseModel(bakeModelLayer(ModelLayers.HORSE_ARMOR));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      HorseRenderer horserenderer = new HorseRenderer(entityrenderdispatcher.getContext());
      horserenderer.model = new HorseModel<>(bakeModelLayer(ModelLayers.HORSE_ARMOR));
      horserenderer.shadowRadius = 0.75F;
      EntityRenderer entityrenderer = rendererCache.get(EntityType.HORSE, index, () -> {
         return horserenderer;
      });
      if (!(entityrenderer instanceof HorseRenderer horserenderer1)) {
         Config.warn("Not a HorseRenderer: " + entityrenderer);
         return null;
      } else {
         HorseArmorLayer horsearmorlayer = new HorseArmorLayer(horserenderer1, entityrenderdispatcher.getContext().getModelSet());
         horsearmorlayer.model = (HorseModel)modelBase;
         horserenderer1.removeLayers(HorseArmorLayer.class);
         horserenderer1.addLayer(horsearmorlayer);
         return horserenderer1;
      }
   }

   public boolean setTextureLocation(IEntityRenderer er, ResourceLocation textureLocation) {
      HorseRenderer horserenderer = (HorseRenderer)er;

      for(HorseArmorLayer horsearmorlayer : horserenderer.getLayers(HorseArmorLayer.class)) {
         horsearmorlayer.customTextureLocation = textureLocation;
      }

      return true;
   }
}