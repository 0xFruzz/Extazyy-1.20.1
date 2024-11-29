package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.HoglinRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.reflect.Reflector;

public class ModelAdapterHoglin extends ModelAdapter {
   private static Map<String, Integer> mapParts = makeMapParts();

   public ModelAdapterHoglin() {
      super(EntityType.HOGLIN, "hoglin", 0.7F);
   }

   public ModelAdapterHoglin(EntityType entityType, String name, float shadowSize) {
      super(entityType, name, shadowSize);
   }

   public Model makeModel() {
      return new HoglinModel(bakeModelLayer(ModelLayers.HOGLIN));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof HoglinModel hoglinmodel)) {
         return null;
      } else if (mapParts.containsKey(modelPart)) {
         int i = mapParts.get(modelPart);
         return (ModelPart)Reflector.getFieldValue(hoglinmodel, Reflector.ModelBoar_ModelRenderers, i);
      } else {
         return null;
      }
   }

   public String[] getModelRendererNames() {
      return mapParts.keySet().toArray(new String[0]);
   }

   private static Map<String, Integer> makeMapParts() {
      Map<String, Integer> map = new LinkedHashMap<>();
      map.put("head", 0);
      map.put("right_ear", 1);
      map.put("left_ear", 2);
      map.put("body", 3);
      map.put("front_right_leg", 4);
      map.put("front_left_leg", 5);
      map.put("back_right_leg", 6);
      map.put("back_left_leg", 7);
      map.put("mane", 8);
      return map;
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      HoglinRenderer hoglinrenderer = new HoglinRenderer(entityrenderdispatcher.getContext());
      hoglinrenderer.model = (HoglinModel)modelBase;
      hoglinrenderer.shadowRadius = shadowSize;
      return hoglinrenderer;
   }
}