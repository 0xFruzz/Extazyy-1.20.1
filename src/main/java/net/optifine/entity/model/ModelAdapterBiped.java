package net.optifine.entity.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EntityType;

public abstract class ModelAdapterBiped extends ModelAdapter {
   public ModelAdapterBiped(EntityType type, String name, float shadowSize) {
      super(type, name, shadowSize);
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof HumanoidModel humanoidmodel)) {
         return null;
      } else if (modelPart.equals("head")) {
         return humanoidmodel.head;
      } else if (modelPart.equals("headwear")) {
         return humanoidmodel.hat;
      } else if (modelPart.equals("body")) {
         return humanoidmodel.body;
      } else if (modelPart.equals("left_arm")) {
         return humanoidmodel.leftArm;
      } else if (modelPart.equals("right_arm")) {
         return humanoidmodel.rightArm;
      } else if (modelPart.equals("left_leg")) {
         return humanoidmodel.leftLeg;
      } else {
         return modelPart.equals("right_leg") ? humanoidmodel.rightLeg : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"head", "headwear", "body", "left_arm", "right_arm", "left_leg", "right_leg"};
   }
}