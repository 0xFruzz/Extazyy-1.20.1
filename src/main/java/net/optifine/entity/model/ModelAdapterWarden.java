package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.WardenRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterWarden extends ModelAdapter {
   private static Map<String, String> mapParts = makeMapParts();

   public ModelAdapterWarden() {
      super(EntityType.WARDEN, "warden", 0.9F);
   }

   public Model makeModel() {
      return new WardenModel(bakeModelLayer(ModelLayers.WARDEN));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof WardenModel wardenmodel)) {
         return null;
      } else if (mapParts.containsKey(modelPart)) {
         String s = mapParts.get(modelPart);
         return wardenmodel.root().getChildModelDeep(s);
      } else {
         return null;
      }
   }

   public String[] getModelRendererNames() {
      return mapParts.keySet().toArray(new String[0]);
   }

   private static Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("body", "bone");
      map.put("torso", "body");
      map.put("head", "head");
      map.put("right_leg", "right_leg");
      map.put("left_leg", "left_leg");
      map.put("right_arm", "right_arm");
      map.put("left_arm", "left_arm");
      map.put("right_tendril", "right_tendril");
      map.put("left_tendril", "left_tendril");
      map.put("right_ribcage", "right_ribcage");
      map.put("left_ribcage", "left_ribcage");
      return map;
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      WardenRenderer wardenrenderer = new WardenRenderer(entityrenderdispatcher.getContext());
      wardenrenderer.model = (WardenModel)modelBase;
      wardenrenderer.shadowRadius = shadowSize;
      return wardenrenderer;
   }
}