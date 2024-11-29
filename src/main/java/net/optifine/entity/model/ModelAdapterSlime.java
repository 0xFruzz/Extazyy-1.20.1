package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterSlime extends ModelAdapter {
   public ModelAdapterSlime() {
      super(EntityType.SLIME, "slime", 0.25F);
   }

   public ModelAdapterSlime(EntityType entityType, String name, float shadowSize) {
      super(entityType, name, shadowSize);
   }

   public Model makeModel() {
      return new SlimeModel(bakeModelLayer(ModelLayers.SLIME));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof SlimeModel slimemodel)) {
         return null;
      } else if (modelPart.equals("body")) {
         return slimemodel.root().getChildModelDeep("cube");
      } else if (modelPart.equals("left_eye")) {
         return slimemodel.root().getChildModelDeep("left_eye");
      } else if (modelPart.equals("right_eye")) {
         return slimemodel.root().getChildModelDeep("right_eye");
      } else {
         return modelPart.equals("mouth") ? slimemodel.root().getChildModelDeep("mouth") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body", "left_eye", "right_eye", "mouth"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      SlimeRenderer slimerenderer = new SlimeRenderer(entityrenderdispatcher.getContext());
      slimerenderer.model = (SlimeModel)modelBase;
      slimerenderer.shadowRadius = shadowSize;
      return slimerenderer;
   }
}