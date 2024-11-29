package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.StriderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.StriderRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterStrider extends ModelAdapter {
   private static Map<String, String> mapParts = makeMapParts();

   public ModelAdapterStrider() {
      super(EntityType.STRIDER, "strider", 0.5F);
   }

   protected ModelAdapterStrider(EntityType entityType, String name, float shadowSize) {
      super(entityType, name, shadowSize);
   }

   public Model makeModel() {
      return new StriderModel(bakeModelLayer(ModelLayers.STRIDER));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof StriderModel stridermodel)) {
         return null;
      } else if (mapParts.containsKey(modelPart)) {
         String s = mapParts.get(modelPart);
         return stridermodel.root().getChildModelDeep(s);
      } else {
         return null;
      }
   }

   public String[] getModelRendererNames() {
      return mapParts.keySet().toArray(new String[0]);
   }

   private static Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("right_leg", "right_leg");
      map.put("left_leg", "left_leg");
      map.put("body", "body");
      map.put("hair_right_bottom", "right_bottom_bristle");
      map.put("hair_right_middle", "right_middle_bristle");
      map.put("hair_right_top", "right_top_bristle");
      map.put("hair_left_top", "left_top_bristle");
      map.put("hair_left_middle", "left_middle_bristle");
      map.put("hair_left_bottom", "left_bottom_bristle");
      return map;
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      StriderRenderer striderrenderer = new StriderRenderer(entityrenderdispatcher.getContext());
      striderrenderer.model = (StriderModel)modelBase;
      striderrenderer.shadowRadius = shadowSize;
      return striderrenderer;
   }
}