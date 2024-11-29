package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.SalmonRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterSalmon extends ModelAdapter {
   public ModelAdapterSalmon() {
      super(EntityType.SALMON, "salmon", 0.3F);
   }

   public Model makeModel() {
      return new SalmonModel(bakeModelLayer(ModelLayers.SALMON));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof SalmonModel salmonmodel)) {
         return null;
      } else if (modelPart.equals("body_front")) {
         return salmonmodel.root().getChildModelDeep("body_front");
      } else if (modelPart.equals("body_back")) {
         return salmonmodel.root().getChildModelDeep("body_back");
      } else if (modelPart.equals("head")) {
         return salmonmodel.root().getChildModelDeep("head");
      } else if (modelPart.equals("fin_back_1")) {
         return salmonmodel.root().getChildModelDeep("top_front_fin");
      } else if (modelPart.equals("fin_back_2")) {
         return salmonmodel.root().getChildModelDeep("top_back_fin");
      } else if (modelPart.equals("tail")) {
         return salmonmodel.root().getChildModelDeep("back_fin");
      } else if (modelPart.equals("fin_right")) {
         return salmonmodel.root().getChildModelDeep("right_fin");
      } else {
         return modelPart.equals("fin_left") ? salmonmodel.root().getChildModelDeep("left_fin") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body_front", "body_back", "head", "fin_back_1", "fin_back_2", "tail", "fin_right", "fin_left"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      SalmonRenderer salmonrenderer = new SalmonRenderer(entityrenderdispatcher.getContext());
      salmonrenderer.model = (SalmonModel)modelBase;
      salmonrenderer.shadowRadius = shadowSize;
      return salmonrenderer;
   }
}