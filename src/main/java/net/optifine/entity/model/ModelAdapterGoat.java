package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.GoatModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.GoatRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterGoat extends ModelAdapterQuadruped {
   public ModelAdapterGoat() {
      super(EntityType.GOAT, "goat", 0.7F);
   }

   public Model makeModel() {
      return new GoatModel(bakeModelLayer(ModelLayers.GOAT));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof GoatModel goatmodel)) {
         return null;
      } else {
         ModelPart modelpart = super.getModelRenderer(goatmodel, "head");
         if (modelpart != null) {
            if (modelPart.equals("left_horn")) {
               return modelpart.getChild(modelPart);
            }

            if (modelPart.equals("right_horn")) {
               return modelpart.getChild(modelPart);
            }

            if (modelPart.equals("nose")) {
               return modelpart.getChild(modelPart);
            }
         }

         return super.getModelRenderer(model, modelPart);
      }
   }

   public String[] getModelRendererNames() {
      String[] astring = super.getModelRendererNames();
      return (String[])Config.addObjectsToArray(astring, new String[]{"left_horn", "right_horn", "nose"});
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      GoatRenderer goatrenderer = new GoatRenderer(entityrenderdispatcher.getContext());
      goatrenderer.model = (GoatModel)modelBase;
      goatrenderer.shadowRadius = shadowSize;
      return goatrenderer;
   }
}