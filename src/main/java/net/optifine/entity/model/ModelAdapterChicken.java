package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.optifine.reflect.Reflector;

public class ModelAdapterChicken extends ModelAdapter {
   public ModelAdapterChicken() {
      super(EntityType.CHICKEN, "chicken", 0.3F);
   }

   public Model makeModel() {
      return new ChickenModel(bakeModelLayer(ModelLayers.CHICKEN));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof ChickenModel chickenmodel)) {
         return null;
      } else if (modelPart.equals("head")) {
         return (ModelPart)Reflector.ModelChicken_ModelRenderers.getValue(chickenmodel, 0);
      } else if (modelPart.equals("body")) {
         return (ModelPart)Reflector.ModelChicken_ModelRenderers.getValue(chickenmodel, 1);
      } else if (modelPart.equals("right_leg")) {
         return (ModelPart)Reflector.ModelChicken_ModelRenderers.getValue(chickenmodel, 2);
      } else if (modelPart.equals("left_leg")) {
         return (ModelPart)Reflector.ModelChicken_ModelRenderers.getValue(chickenmodel, 3);
      } else if (modelPart.equals("right_wing")) {
         return (ModelPart)Reflector.ModelChicken_ModelRenderers.getValue(chickenmodel, 4);
      } else if (modelPart.equals("left_wing")) {
         return (ModelPart)Reflector.ModelChicken_ModelRenderers.getValue(chickenmodel, 5);
      } else if (modelPart.equals("bill")) {
         return (ModelPart)Reflector.ModelChicken_ModelRenderers.getValue(chickenmodel, 6);
      } else {
         return modelPart.equals("chin") ? (ModelPart)Reflector.ModelChicken_ModelRenderers.getValue(chickenmodel, 7) : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"head", "body", "right_leg", "left_leg", "right_wing", "left_wing", "bill", "chin"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      ChickenRenderer chickenrenderer = new ChickenRenderer(entityrenderdispatcher.getContext());
      chickenrenderer.model = (ChickenModel)modelBase;
      chickenrenderer.shadowRadius = shadowSize;
      return chickenrenderer;
   }
}