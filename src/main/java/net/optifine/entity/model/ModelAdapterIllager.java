package net.optifine.entity.model;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EntityType;

public abstract class ModelAdapterIllager extends ModelAdapter {
   public ModelAdapterIllager(EntityType type, String name, float shadowSize) {
      super(type, name, shadowSize);
   }

   public ModelAdapterIllager(EntityType type, String name, float shadowSize, String[] aliases) {
      super(type, name, shadowSize, aliases);
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof IllagerModel illagermodel)) {
         return null;
      } else if (modelPart.equals("head")) {
         return illagermodel.root().getChildModelDeep("head");
      } else if (modelPart.equals("hat")) {
         return illagermodel.root().getChildModelDeep("hat");
      } else if (modelPart.equals("body")) {
         return illagermodel.root().getChildModelDeep("body");
      } else if (modelPart.equals("arms")) {
         return illagermodel.root().getChildModelDeep("arms");
      } else if (modelPart.equals("right_leg")) {
         return illagermodel.root().getChildModelDeep("right_leg");
      } else if (modelPart.equals("left_leg")) {
         return illagermodel.root().getChildModelDeep("left_leg");
      } else if (modelPart.equals("nose")) {
         return illagermodel.root().getChildModelDeep("nose");
      } else if (modelPart.equals("right_arm")) {
         return illagermodel.root().getChildModelDeep("right_arm");
      } else {
         return modelPart.equals("left_arm") ? illagermodel.root().getChildModelDeep("left_arm") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"head", "hat", "body", "arms", "right_leg", "left_leg", "nose", "right_arm", "left_arm"};
   }
}