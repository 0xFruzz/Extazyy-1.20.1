package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AxolotlRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.optifine.reflect.Reflector;

public class ModelAdapterAxolotl extends ModelAdapter {
   private static Map<String, Integer> mapPartFields = null;

   public ModelAdapterAxolotl() {
      super(EntityType.AXOLOTL, "axolotl", 0.5F);
   }

   public Model makeModel() {
      return new AxolotlModel(bakeModelLayer(ModelLayers.AXOLOTL));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof AxolotlModel axolotlmodel)) {
         return null;
      } else {
         Map<String, Integer> map = getMapPartFields();
         if (map.containsKey(modelPart)) {
            int i = map.get(modelPart);
            return (ModelPart)Reflector.getFieldValue(axolotlmodel, Reflector.ModelAxolotl_ModelRenderers, i);
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
         mapPartFields.put("tail", 0);
         mapPartFields.put("leg2", 1);
         mapPartFields.put("leg1", 2);
         mapPartFields.put("leg4", 3);
         mapPartFields.put("leg3", 4);
         mapPartFields.put("body", 5);
         mapPartFields.put("head", 6);
         mapPartFields.put("top_gills", 7);
         mapPartFields.put("left_gills", 8);
         mapPartFields.put("right_gills", 9);
         return mapPartFields;
      }
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      AxolotlRenderer axolotlrenderer = new AxolotlRenderer(entityrenderdispatcher.getContext());
      axolotlrenderer.model = (AxolotlModel)modelBase;
      axolotlrenderer.shadowRadius = shadowSize;
      return axolotlrenderer;
   }
}