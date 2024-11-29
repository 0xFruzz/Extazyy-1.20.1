package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.reflect.Reflector;

public class ModelAdapterHorse extends ModelAdapter {
   private static Map<String, Integer> mapParts = makeMapParts();
   private static Map<String, String> mapPartsNeck = makeMapPartsNeck();
   private static Map<String, String> mapPartsHead = makeMapPartsHead();
   private static Map<String, String> mapPartsBody = makeMapPartsBody();

   public ModelAdapterHorse() {
      super(EntityType.HORSE, "horse", 0.75F);
   }

   protected ModelAdapterHorse(EntityType type, String name, float shadowSize) {
      super(type, name, shadowSize);
   }

   public Model makeModel() {
      return new HorseModel(bakeModelLayer(ModelLayers.HORSE));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof HorseModel horsemodel)) {
         return null;
      } else if (mapParts.containsKey(modelPart)) {
         int i = mapParts.get(modelPart);
         return (ModelPart)Reflector.getFieldValue(horsemodel, Reflector.ModelHorse_ModelRenderers, i);
      } else if (mapPartsNeck.containsKey(modelPart)) {
         ModelPart modelpart2 = this.getModelRenderer(horsemodel, "neck");
         String s2 = mapPartsNeck.get(modelPart);
         return modelpart2.getChild(s2);
      } else if (mapPartsHead.containsKey(modelPart)) {
         ModelPart modelpart1 = this.getModelRenderer(horsemodel, "head");
         String s1 = mapPartsHead.get(modelPart);
         return modelpart1.getChild(s1);
      } else if (mapPartsBody.containsKey(modelPart)) {
         ModelPart modelpart = this.getModelRenderer(horsemodel, "body");
         String s = mapPartsBody.get(modelPart);
         return modelpart.getChild(s);
      } else {
         return null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body", "neck", "back_left_leg", "back_right_leg", "front_left_leg", "front_right_leg", "child_back_left_leg", "child_back_right_leg", "child_front_left_leg", "child_front_right_leg", "tail", "saddle", "head", "mane", "mouth", "left_ear", "right_ear", "left_bit", "right_bit", "left_rein", "right_rein", "headpiece", "noseband"};
   }

   private static Map<String, Integer> makeMapParts() {
      Map<String, Integer> map = new LinkedHashMap<>();
      map.put("body", 0);
      map.put("neck", 1);
      map.put("back_right_leg", 2);
      map.put("back_left_leg", 3);
      map.put("front_right_leg", 4);
      map.put("front_left_leg", 5);
      map.put("child_back_right_leg", 6);
      map.put("child_back_left_leg", 7);
      map.put("child_front_right_leg", 8);
      map.put("child_front_left_leg", 9);
      return map;
   }

   private static Map<String, String> makeMapPartsNeck() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("head", "head");
      map.put("mane", "mane");
      map.put("mouth", "upper_mouth");
      map.put("left_bit", "left_saddle_mouth");
      map.put("right_bit", "right_saddle_mouth");
      map.put("left_rein", "left_saddle_line");
      map.put("right_rein", "right_saddle_line");
      map.put("headpiece", "head_saddle");
      map.put("noseband", "mouth_saddle_wrap");
      return map;
   }

   private static Map<String, String> makeMapPartsBody() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("tail", "tail");
      map.put("saddle", "saddle");
      return map;
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      HorseRenderer horserenderer = new HorseRenderer(entityrenderdispatcher.getContext());
      horserenderer.model = (HorseModel)modelBase;
      horserenderer.shadowRadius = shadowSize;
      return horserenderer;
   }

   private static Map<String, String> makeMapPartsHead() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("left_ear", "left_ear");
      map.put("right_ear", "right_ear");
      return map;
   }
}