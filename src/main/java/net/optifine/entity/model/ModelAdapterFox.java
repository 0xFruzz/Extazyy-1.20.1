package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.reflect.Reflector;

public class ModelAdapterFox extends ModelAdapter {
   private static Map<String, Integer> mapPartFields = null;

   public ModelAdapterFox() {
      super(EntityType.FOX, "fox", 0.4F);
   }

   public Model makeModel() {
      return new FoxModel(bakeModelLayer(ModelLayers.FOX));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof FoxModel foxmodel)) {
         return null;
      } else {
         Map<String, Integer> map = getMapPartFields();
         if (map.containsKey(modelPart)) {
            int i = map.get(modelPart);
            return (ModelPart)Reflector.getFieldValue(foxmodel, Reflector.ModelFox_ModelRenderers, i);
         } else {
            return null;
         }
      }
   }

   public String[] getModelRendererNames() {
      return getMapPartFields().keySet().toArray(new String[0]);
   }

   private static Map<String, Integer> getMapPartFields() {
      if (mapPartFields != null) {
         return mapPartFields;
      } else {
         mapPartFields = new LinkedHashMap<>();
         mapPartFields.put("head", 0);
         mapPartFields.put("body", 1);
         mapPartFields.put("leg1", 2);
         mapPartFields.put("leg2", 3);
         mapPartFields.put("leg3", 4);
         mapPartFields.put("leg4", 5);
         mapPartFields.put("tail", 6);
         return mapPartFields;
      }
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      FoxRenderer foxrenderer = new FoxRenderer(entityrenderdispatcher.getContext());
      foxrenderer.model = (FoxModel)modelBase;
      foxrenderer.shadowRadius = shadowSize;
      return foxrenderer;
   }
}