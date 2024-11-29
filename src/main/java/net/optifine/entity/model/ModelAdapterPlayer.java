package net.optifine.entity.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EntityType;

public abstract class ModelAdapterPlayer extends ModelAdapterBiped {
   protected ModelAdapterPlayer(EntityType type, String name, float shadowSize) {
      super(type, name, shadowSize);
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (model instanceof PlayerModel playermodel) {
         if (modelPart.equals("left_sleeve")) {
            return playermodel.leftSleeve;
         }

         if (modelPart.equals("right_sleeve")) {
            return playermodel.rightSleeve;
         }

         if (modelPart.equals("left_pants")) {
            return playermodel.leftPants;
         }

         if (modelPart.equals("right_pants")) {
            return playermodel.rightPants;
         }

         if (modelPart.equals("jacket")) {
            return playermodel.jacket;
         }
      }

      return super.getModelRenderer(model, modelPart);
   }

   public String[] getModelRendererNames() {
      List<String> list = new ArrayList<>(Arrays.asList(super.getModelRendererNames()));
      list.add("left_sleeve");
      list.add("right_sleeve");
      list.add("left_pants");
      list.add("right_pants");
      list.add("jacket");
      return list.toArray(new String[list.size()]);
   }
}