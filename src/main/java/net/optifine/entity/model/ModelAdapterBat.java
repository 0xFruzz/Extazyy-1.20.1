package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterBat extends ModelAdapter {
   public ModelAdapterBat() {
      super(EntityType.BAT, "bat", 0.25F);
   }

   public Model makeModel() {
      return new BatModel(bakeModelLayer(ModelLayers.BAT));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof BatModel batmodel)) {
         return null;
      } else if (modelPart.equals("head")) {
         return batmodel.root().getChildModelDeep("head");
      } else if (modelPart.equals("body")) {
         return batmodel.root().getChildModelDeep("body");
      } else if (modelPart.equals("right_wing")) {
         return batmodel.root().getChildModelDeep("right_wing");
      } else if (modelPart.equals("left_wing")) {
         return batmodel.root().getChildModelDeep("left_wing");
      } else if (modelPart.equals("outer_right_wing")) {
         return batmodel.root().getChildModelDeep("right_wing_tip");
      } else {
         return modelPart.equals("outer_left_wing") ? batmodel.root().getChildModelDeep("left_wing_tip") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"head", "body", "right_wing", "left_wing", "outer_right_wing", "outer_left_wing"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      BatRenderer batrenderer = new BatRenderer(entityrenderdispatcher.getContext());
      batrenderer.model = (BatModel)modelBase;
      batrenderer.shadowRadius = shadowSize;
      return batrenderer;
   }
}