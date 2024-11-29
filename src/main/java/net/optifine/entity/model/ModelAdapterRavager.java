package net.optifine.entity.model;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.RavagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RavagerRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterRavager extends ModelAdapter {
   private static Map<String, String> mapPartFields = null;

   public ModelAdapterRavager() {
      super(EntityType.RAVAGER, "ravager", 1.1F);
   }

   public Model makeModel() {
      return new RavagerModel(bakeModelLayer(ModelLayers.RAVAGER));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof RavagerModel ravagermodel)) {
         return null;
      } else {
         Map<String, String> map = getMapPartFields();
         if (map.containsKey(modelPart)) {
            String s = map.get(modelPart);
            return ravagermodel.root().getChildModelDeep(s);
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
         mapPartFields.put("head", "head");
         mapPartFields.put("jaw", "mouth");
         mapPartFields.put("body", "body");
         mapPartFields.put("leg1", "right_hind_leg");
         mapPartFields.put("leg2", "left_hind_leg");
         mapPartFields.put("leg3", "right_front_leg");
         mapPartFields.put("leg4", "left_front_leg");
         mapPartFields.put("neck", "neck");
         return mapPartFields;
      }
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      RavagerRenderer ravagerrenderer = new RavagerRenderer(entityrenderdispatcher.getContext());
      ravagerrenderer.model = (RavagerModel)modelBase;
      ravagerrenderer.shadowRadius = shadowSize;
      return ravagerrenderer;
   }
}