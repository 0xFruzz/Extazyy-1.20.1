package net.optifine.entity.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.reflect.Reflector;

public class ModelAdapterPiglin extends ModelAdapterPlayer {
   public ModelAdapterPiglin() {
      super(EntityType.PIGLIN, "piglin", 0.5F);
   }

   protected ModelAdapterPiglin(EntityType type, String name, float shadowSize) {
      super(type, name, shadowSize);
   }

   public Model makeModel() {
      return new PiglinModel(bakeModelLayer(ModelLayers.PIGLIN));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (model instanceof PiglinModel piglinmodel) {
         if (Reflector.ModelPiglin_ModelRenderers.exists()) {
            if (modelPart.equals("left_ear")) {
               return piglinmodel.head.getChild("left_ear");
            }

            if (modelPart.equals("right_ear")) {
               return piglinmodel.head.getChild("right_ear");
            }
         }
      }

      return super.getModelRenderer(model, modelPart);
   }

   public String[] getModelRendererNames() {
      List<String> list = new ArrayList<>(Arrays.asList(super.getModelRendererNames()));
      list.add("left_ear");
      list.add("right_ear");
      return list.toArray(new String[list.size()]);
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      PiglinRenderer piglinrenderer = new PiglinRenderer(entityrenderdispatcher.getContext(), ModelLayers.PIGLIN, ModelLayers.PIGLIN_INNER_ARMOR, ModelLayers.PIGLIN_OUTER_ARMOR, false);
      piglinrenderer.model = (PiglinModel)modelBase;
      piglinrenderer.shadowRadius = shadowSize;
      return piglinrenderer;
   }
}