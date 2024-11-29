package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterSpider extends ModelAdapter {
   public ModelAdapterSpider() {
      super(EntityType.SPIDER, "spider", 1.0F);
   }

   protected ModelAdapterSpider(EntityType type, String name, float shadowSize) {
      super(type, name, shadowSize);
   }

   public Model makeModel() {
      return new SpiderModel(bakeModelLayer(ModelLayers.SPIDER));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof SpiderModel spidermodel)) {
         return null;
      } else if (modelPart.equals("head")) {
         return spidermodel.root().getChildModelDeep("head");
      } else if (modelPart.equals("neck")) {
         return spidermodel.root().getChildModelDeep("body0");
      } else if (modelPart.equals("body")) {
         return spidermodel.root().getChildModelDeep("body1");
      } else if (modelPart.equals("leg1")) {
         return spidermodel.root().getChildModelDeep("right_hind_leg");
      } else if (modelPart.equals("leg2")) {
         return spidermodel.root().getChildModelDeep("left_hind_leg");
      } else if (modelPart.equals("leg3")) {
         return spidermodel.root().getChildModelDeep("right_middle_hind_leg");
      } else if (modelPart.equals("leg4")) {
         return spidermodel.root().getChildModelDeep("left_middle_hind_leg");
      } else if (modelPart.equals("leg5")) {
         return spidermodel.root().getChildModelDeep("right_middle_front_leg");
      } else if (modelPart.equals("leg6")) {
         return spidermodel.root().getChildModelDeep("left_middle_front_leg");
      } else if (modelPart.equals("leg7")) {
         return spidermodel.root().getChildModelDeep("right_front_leg");
      } else {
         return modelPart.equals("leg8") ? spidermodel.root().getChildModelDeep("left_front_leg") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"head", "neck", "body", "leg1", "leg2", "leg3", "leg4", "leg5", "leg6", "leg7", "leg8"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      SpiderRenderer spiderrenderer = new SpiderRenderer(entityrenderdispatcher.getContext());
      spiderrenderer.model = (EntityModel)modelBase;
      spiderrenderer.shadowRadius = shadowSize;
      return spiderrenderer;
   }
}