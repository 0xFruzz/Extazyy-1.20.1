package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterIronGolem extends ModelAdapter {
   public ModelAdapterIronGolem() {
      super(EntityType.IRON_GOLEM, "iron_golem", 0.5F);
   }

   public Model makeModel() {
      return new IronGolemModel(bakeModelLayer(ModelLayers.IRON_GOLEM));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof IronGolemModel irongolemmodel)) {
         return null;
      } else if (modelPart.equals("head")) {
         return irongolemmodel.root().getChild("head");
      } else if (modelPart.equals("body")) {
         return irongolemmodel.root().getChild("body");
      } else if (modelPart.equals("right_arm")) {
         return irongolemmodel.root().getChild("right_arm");
      } else if (modelPart.equals("left_arm")) {
         return irongolemmodel.root().getChild("left_arm");
      } else if (modelPart.equals("left_leg")) {
         return irongolemmodel.root().getChild("left_leg");
      } else {
         return modelPart.equals("right_leg") ? irongolemmodel.root().getChild("right_leg") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"head", "body", "right_arm", "left_arm", "left_leg", "right_leg"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      IronGolemRenderer irongolemrenderer = new IronGolemRenderer(entityrenderdispatcher.getContext());
      irongolemrenderer.model = (IronGolemModel)modelBase;
      irongolemrenderer.shadowRadius = shadowSize;
      return irongolemrenderer;
   }
}