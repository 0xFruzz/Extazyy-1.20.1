package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.VexModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterVex extends ModelAdapter {
   private static Map<String, String> mapParts = makeMapParts();

   public ModelAdapterVex() {
      super(EntityType.VEX, "vex", 0.3F);
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof VexModel vexmodel)) {
         return null;
      } else if (mapParts.containsKey(modelPart)) {
         String s = mapParts.get(modelPart);
         return vexmodel.root().getChildModelDeep(s);
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

   public Model makeModel() {
      return new VexModel(bakeModelLayer(ModelLayers.VEX));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      VexRenderer vexrenderer = new VexRenderer(entityrenderdispatcher.getContext());
      vexrenderer.model = (VexModel)modelBase;
      vexrenderer.shadowRadius = shadowSize;
      return vexrenderer;
   }
}