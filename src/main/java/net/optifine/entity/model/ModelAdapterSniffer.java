package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SnifferModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.SnifferRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterSniffer extends ModelAdapter {
   private static Map<String, String> mapParts = makeMapParts();

   public ModelAdapterSniffer() {
      super(EntityType.SNIFFER, "sniffer", 1.1F);
   }

   public Model makeModel() {
      return new SnifferModel(bakeModelLayer(ModelLayers.SNIFFER));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof SnifferModel sniffermodel)) {
         return null;
      } else if (mapParts.containsKey(modelPart)) {
         String s = mapParts.get(modelPart);
         return sniffermodel.root().getChildModelDeep(s);
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
      map.put("back_left_leg", "left_hind_leg");
      map.put("back_right_leg", "right_hind_leg");
      map.put("middle_left_leg", "left_mid_leg");
      map.put("middle_right_leg", "right_mid_leg");
      map.put("front_left_leg", "left_front_leg");
      map.put("front_right_leg", "right_front_leg");
      map.put("head", "head");
      map.put("left_ear", "left_ear");
      map.put("right_ear", "right_ear");
      map.put("nose", "nose");
      map.put("lower_beak", "lower_beak");
      return map;
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      SnifferRenderer snifferrenderer = new SnifferRenderer(entityrenderdispatcher.getContext());
      snifferrenderer.model = (SnifferModel)modelBase;
      snifferrenderer.shadowRadius = shadowSize;
      return snifferrenderer;
   }
}