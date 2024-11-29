package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterTrident extends ModelAdapter {
   public ModelAdapterTrident() {
      super(EntityType.TRIDENT, "trident", 0.0F);
   }

   public Model makeModel() {
      return new TridentModel(bakeModelLayer(ModelLayers.TRIDENT));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof TridentModel tridentmodel)) {
         return null;
      } else {
         if (modelPart.equals("body")) {
            ModelPart modelpart = (ModelPart)Reflector.ModelTrident_root.getValue(tridentmodel);
            if (modelpart != null) {
               return modelpart.getChildModelDeep("pole");
            }
         }

         return null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      ThrownTridentRenderer throwntridentrenderer = new ThrownTridentRenderer(entityrenderdispatcher.getContext());
      if (!Reflector.RenderTrident_modelTrident.exists()) {
         Config.warn("Field not found: RenderTrident.modelTrident");
         return null;
      } else {
         Reflector.setFieldValue(throwntridentrenderer, Reflector.RenderTrident_modelTrident, modelBase);
         throwntridentrenderer.shadowRadius = shadowSize;
         return throwntridentrenderer;
      }
   }
}