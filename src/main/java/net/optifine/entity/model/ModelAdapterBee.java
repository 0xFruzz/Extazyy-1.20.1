package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.optifine.reflect.Reflector;

public class ModelAdapterBee extends ModelAdapter {
   private static Map<String, String> mapParts = makeMapParts();

   public ModelAdapterBee() {
      super(EntityType.BEE, "bee", 0.4F);
   }

   public Model makeModel() {
      return new BeeModel(bakeModelLayer(ModelLayers.BEE));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof BeeModel beemodel)) {
         return null;
      } else if (modelPart.equals("body")) {
         return (ModelPart)Reflector.getFieldValue(beemodel, Reflector.ModelBee_ModelRenderers, 0);
      } else if (mapParts.containsKey(modelPart)) {
         String s = mapParts.get(modelPart);
         ModelPart modelpart = this.getModelRenderer(beemodel, "body");
         return modelpart.getChildModelDeep(s);
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
      map.put("right_wing", "right_wing");
      map.put("left_wing", "left_wing");
      map.put("front_legs", "front_legs");
      map.put("middle_legs", "middle_legs");
      map.put("back_legs", "back_legs");
      map.put("stinger", "stinger");
      map.put("left_antenna", "left_antenna");
      map.put("right_antenna", "right_antenna");
      return map;
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      BeeRenderer beerenderer = new BeeRenderer(entityrenderdispatcher.getContext());
      beerenderer.model = (BeeModel)modelBase;
      beerenderer.shadowRadius = shadowSize;
      return beerenderer;
   }
}