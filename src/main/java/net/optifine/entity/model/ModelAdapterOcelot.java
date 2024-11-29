package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.OcelotRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.reflect.Reflector;

public class ModelAdapterOcelot extends ModelAdapter {
   private static Map<String, Integer> mapPartFields = null;

   public ModelAdapterOcelot() {
      super(EntityType.OCELOT, "ocelot", 0.4F);
   }

   protected ModelAdapterOcelot(EntityType type, String name, float shadowSize) {
      super(type, name, shadowSize);
   }

   public Model makeModel() {
      return new OcelotModel(bakeModelLayer(ModelLayers.OCELOT));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof OcelotModel ocelotmodel)) {
         return null;
      } else {
         Map<String, Integer> map = getMapPartFields();
         if (map.containsKey(modelPart)) {
            int i = map.get(modelPart);
            return (ModelPart)Reflector.getFieldValue(ocelotmodel, Reflector.ModelOcelot_ModelRenderers, i);
         } else {
            return null;
         }
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"back_left_leg", "back_right_leg", "front_left_leg", "front_right_leg", "tail", "tail2", "head", "body"};
   }

   private static Map<String, Integer> getMapPartFields() {
      if (mapPartFields != null) {
         return mapPartFields;
      } else {
         mapPartFields = new LinkedHashMap<>();
         mapPartFields.put("back_left_leg", 0);
         mapPartFields.put("back_right_leg", 1);
         mapPartFields.put("front_left_leg", 2);
         mapPartFields.put("front_right_leg", 3);
         mapPartFields.put("tail", 4);
         mapPartFields.put("tail2", 5);
         mapPartFields.put("head", 6);
         mapPartFields.put("body", 7);
         return mapPartFields;
      }
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      OcelotRenderer ocelotrenderer = new OcelotRenderer(entityrenderdispatcher.getContext());
      ocelotrenderer.model = (OcelotModel)modelBase;
      ocelotrenderer.shadowRadius = shadowSize;
      return ocelotrenderer;
   }
}