package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterDragon extends ModelAdapter {
   public ModelAdapterDragon() {
      super(EntityType.ENDER_DRAGON, "dragon", 0.5F);
   }

   public Model makeModel() {
      return new EnderDragonRenderer.DragonModel(bakeModelLayer(ModelLayers.ENDER_DRAGON));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof EnderDragonRenderer.DragonModel enderdragonrenderer$dragonmodel)) {
         return null;
      } else if (modelPart.equals("head")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 0);
      } else if (modelPart.equals("spine")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 1);
      } else if (modelPart.equals("jaw")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 2);
      } else if (modelPart.equals("body")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 3);
      } else if (modelPart.equals("left_wing")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 4);
      } else if (modelPart.equals("left_wing_tip")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 5);
      } else if (modelPart.equals("front_left_leg")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 6);
      } else if (modelPart.equals("front_left_shin")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 7);
      } else if (modelPart.equals("front_left_foot")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 8);
      } else if (modelPart.equals("back_left_leg")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 9);
      } else if (modelPart.equals("back_left_shin")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 10);
      } else if (modelPart.equals("back_left_foot")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 11);
      } else if (modelPart.equals("right_wing")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 12);
      } else if (modelPart.equals("right_wing_tip")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 13);
      } else if (modelPart.equals("front_right_leg")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 14);
      } else if (modelPart.equals("front_right_shin")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 15);
      } else if (modelPart.equals("front_right_foot")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 16);
      } else if (modelPart.equals("back_right_leg")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 17);
      } else if (modelPart.equals("back_right_shin")) {
         return (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 18);
      } else {
         return modelPart.equals("back_right_foot") ? (ModelPart)Reflector.getFieldValue(enderdragonrenderer$dragonmodel, Reflector.ModelDragon_ModelRenderers, 19) : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"head", "spine", "jaw", "body", "left_wing", "left_wing_tip", "front_left_leg", "front_left_shin", "front_left_foot", "back_left_leg", "back_left_shin", "back_left_foot", "right_wing", "right_wing_tip", "front_right_leg", "front_right_shin", "front_right_foot", "back_right_leg", "back_right_shin", "back_right_foot"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      EnderDragonRenderer enderdragonrenderer = new EnderDragonRenderer(entityrenderdispatcher.getContext());
      if (!Reflector.EnderDragonRenderer_model.exists()) {
         Config.warn("Field not found: EnderDragonRenderer.model");
         return null;
      } else {
         Reflector.setFieldValue(enderdragonrenderer, Reflector.EnderDragonRenderer_model, modelBase);
         enderdragonrenderer.shadowRadius = shadowSize;
         return enderdragonrenderer;
      }
   }
}