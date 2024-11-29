package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.DolphinRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterDolphin extends ModelAdapter {
   public ModelAdapterDolphin() {
      super(EntityType.DOLPHIN, "dolphin", 0.7F);
   }

   public Model makeModel() {
      return new DolphinModel(bakeModelLayer(ModelLayers.DOLPHIN));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof DolphinModel dolphinmodel)) {
         return null;
      } else {
         ModelPart modelpart = dolphinmodel.root().getChild("body");
         if (modelpart == null) {
            return null;
         } else if (modelPart.equals("body")) {
            return modelpart;
         } else if (modelPart.equals("back_fin")) {
            return modelpart.getChild("back_fin");
         } else if (modelPart.equals("left_fin")) {
            return modelpart.getChild("left_fin");
         } else if (modelPart.equals("right_fin")) {
            return modelpart.getChild("right_fin");
         } else if (modelPart.equals("tail")) {
            return modelpart.getChild("tail");
         } else if (modelPart.equals("tail_fin")) {
            return modelpart.getChild("tail").getChild("tail_fin");
         } else {
            return modelPart.equals("head") ? modelpart.getChild("head") : null;
         }
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body", "back_fin", "left_fin", "right_fin", "tail", "tail_fin", "head"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      DolphinRenderer dolphinrenderer = new DolphinRenderer(entityrenderdispatcher.getContext());
      dolphinrenderer.model = (DolphinModel)modelBase;
      dolphinrenderer.shadowRadius = shadowSize;
      return dolphinrenderer;
   }
}