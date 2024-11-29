package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EvokerFangsRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterEvokerFangs extends ModelAdapter {
   public ModelAdapterEvokerFangs() {
      super(EntityType.EVOKER_FANGS, "evoker_fangs", 0.0F, new String[]{"evocation_fangs"});
   }

   public Model makeModel() {
      return new EvokerFangsModel(bakeModelLayer(ModelLayers.EVOKER_FANGS));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof EvokerFangsModel evokerfangsmodel)) {
         return null;
      } else if (modelPart.equals("base")) {
         return evokerfangsmodel.root().getChildModelDeep("base");
      } else if (modelPart.equals("upper_jaw")) {
         return evokerfangsmodel.root().getChildModelDeep("upper_jaw");
      } else {
         return modelPart.equals("lower_jaw") ? evokerfangsmodel.root().getChildModelDeep("lower_jaw") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"base", "upper_jaw", "lower_jaw"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      EvokerFangsRenderer evokerfangsrenderer = new EvokerFangsRenderer(entityrenderdispatcher.getContext());
      if (!Reflector.RenderEvokerFangs_model.exists()) {
         Config.warn("Field not found: RenderEvokerFangs.model");
         return null;
      } else {
         Reflector.setFieldValue(evokerfangsrenderer, Reflector.RenderEvokerFangs_model, modelBase);
         evokerfangsrenderer.shadowRadius = shadowSize;
         return evokerfangsrenderer;
      }
   }
}