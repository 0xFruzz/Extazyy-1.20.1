package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CamelModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.CamelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterCamel extends ModelAdapter {
   private static Map<String, String> mapParts = makeMapParts();

   public ModelAdapterCamel() {
      super(EntityType.CAMEL, "camel", 0.7F);
   }

   public Model makeModel() {
      return new CamelModel(bakeModelLayer(ModelLayers.CAMEL));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof CamelModel camelmodel)) {
         return null;
      } else if (mapParts.containsKey(modelPart)) {
         String s = mapParts.get(modelPart);
         return camelmodel.root().getChildModelDeep(s);
      } else {
         return null;
      }
   }

   public String[] getModelRendererNames() {
      return mapParts.keySet().toArray(new String[0]);
   }

   private static Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("body", "body");
      map.put("hump", "hump");
      map.put("tail", "tail");
      map.put("head", "head");
      map.put("left_ear", "left_ear");
      map.put("right_ear", "right_ear");
      map.put("back_left_leg", "left_hind_leg");
      map.put("back_right_leg", "right_hind_leg");
      map.put("front_left_leg", "left_front_leg");
      map.put("front_right_leg", "right_front_leg");
      map.put("saddle", "saddle");
      map.put("reins", "reins");
      map.put("bridle", "bridle");
      return map;
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      CamelRenderer camelrenderer = new CamelRenderer(entityrenderdispatcher.getContext(), ModelLayers.CAMEL);
      camelrenderer.model = (CamelModel)modelBase;
      camelrenderer.shadowRadius = shadowSize;
      return camelrenderer;
   }
}