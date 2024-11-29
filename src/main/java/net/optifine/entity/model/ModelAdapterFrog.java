package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.FrogRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterFrog extends ModelAdapter {
   private static Map<String, String> mapParts = makeMapParts();

   public ModelAdapterFrog() {
      super(EntityType.FROG, "frog", 0.3F);
   }

   public Model makeModel() {
      return new FrogModel(bakeModelLayer(ModelLayers.FROG));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof FrogModel frogmodel)) {
         return null;
      } else if (mapParts.containsKey(modelPart)) {
         String s = mapParts.get(modelPart);
         return frogmodel.root().getChildModelDeep(s);
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
      map.put("eyes", "eyes");
      map.put("tongue", "tongue");
      map.put("left_arm", "left_arm");
      map.put("right_arm", "right_arm");
      map.put("left_leg", "left_leg");
      map.put("right_leg", "right_leg");
      map.put("croaking_body", "croaking_body");
      return map;
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      FrogRenderer frogrenderer = new FrogRenderer(entityrenderdispatcher.getContext());
      frogrenderer.model = (FrogModel)modelBase;
      frogrenderer.shadowRadius = shadowSize;
      return frogrenderer;
   }
}