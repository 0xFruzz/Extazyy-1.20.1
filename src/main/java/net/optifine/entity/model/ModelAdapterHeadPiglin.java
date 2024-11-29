package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.PiglinHeadModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.level.block.SkullBlock;
import net.optifine.reflect.Reflector;

public class ModelAdapterHeadPiglin extends ModelAdapterHead {
   public ModelAdapterHeadPiglin() {
      super("head_piglin", (ModelLayerLocation)null, SkullBlock.Types.PIGLIN);
   }

   public Model makeModel() {
      return new PiglinHeadModel(bakeModelLayer(ModelLayers.PIGLIN_HEAD));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof PiglinHeadModel piglinheadmodel)) {
         return null;
      } else if (modelPart.equals("head")) {
         return (ModelPart)Reflector.ModelPiglinHead_ModelRenderers.getValue(piglinheadmodel, 0);
      } else if (modelPart.equals("left_ear")) {
         return (ModelPart)Reflector.ModelPiglinHead_ModelRenderers.getValue(piglinheadmodel, 1);
      } else {
         return modelPart.equals("right_ear") ? (ModelPart)Reflector.ModelPiglinHead_ModelRenderers.getValue(piglinheadmodel, 2) : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"head", "left_ear", "right_ear"};
   }
}