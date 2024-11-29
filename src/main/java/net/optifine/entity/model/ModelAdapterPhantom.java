package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.PhantomRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterPhantom extends ModelAdapter {
   private static Map<String, String> mapPartFields = null;

   public ModelAdapterPhantom() {
      super(EntityType.PHANTOM, "phantom", 0.75F);
   }

   public Model makeModel() {
      return new PhantomModel(bakeModelLayer(ModelLayers.PHANTOM));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof PhantomModel phantommodel)) {
         return null;
      } else {
         Map<String, String> map = getMapPartFields();
         if (map.containsKey(modelPart)) {
            String s = map.get(modelPart);
            return phantommodel.root().getChildModelDeep(s);
         } else {
            return null;
         }
      }
   }

   public String[] getModelRendererNames() {
      return getMapPartFields().keySet().toArray(new String[0]);
   }

   private static Map<String, String> getMapPartFields() {
      if (mapPartFields != null) {
         return mapPartFields;
      } else {
         mapPartFields = new LinkedHashMap<>();
         mapPartFields.put("body", "body");
         mapPartFields.put("head", "head");
         mapPartFields.put("left_wing", "left_wing_base");
         mapPartFields.put("left_wing_tip", "left_wing_tip");
         mapPartFields.put("right_wing", "right_wing_base");
         mapPartFields.put("right_wing_tip", "right_wing_tip");
         mapPartFields.put("tail", "tail_base");
         mapPartFields.put("tail2", "tail_tip");
         return mapPartFields;
      }
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      PhantomRenderer phantomrenderer = new PhantomRenderer(entityrenderdispatcher.getContext());
      phantomrenderer.model = (PhantomModel)modelBase;
      phantomrenderer.shadowRadius = shadowSize;
      return phantomrenderer;
   }
}