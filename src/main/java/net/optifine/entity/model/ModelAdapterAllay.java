package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.AllayRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterAllay extends ModelAdapter {
   private static Map<String, String> mapParts = makeMapParts();

   public ModelAdapterAllay() {
      super(EntityType.ALLAY, "allay", 0.4F);
   }

   public Model makeModel() {
      return new AllayModel(bakeModelLayer(ModelLayers.ALLAY));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof AllayModel allaymodel)) {
         return null;
      } else if (mapParts.containsKey(modelPart)) {
         String s = mapParts.get(modelPart);
         return allaymodel.root().getChildModelDeep(s);
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
      map.put("head", "head");
      map.put("right_arm", "right_arm");
      map.put("left_arm", "left_arm");
      map.put("right_wing", "right_wing");
      map.put("left_wing", "left_wing");
      return map;
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      AllayRenderer allayrenderer = new AllayRenderer(entityrenderdispatcher.getContext());
      allayrenderer.model = (AllayModel)modelBase;
      allayrenderer.shadowRadius = shadowSize;
      return allayrenderer;
   }
}