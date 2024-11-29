package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.TadpoleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.TadpoleRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.reflect.Reflector;

public class ModelAdapterTadpole extends ModelAdapter {
   public ModelAdapterTadpole() {
      super(EntityType.TADPOLE, "tadpole", 0.14F);
   }

   public Model makeModel() {
      return new TadpoleModel(bakeModelLayer(ModelLayers.TADPOLE));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof TadpoleModel tadpolemodel)) {
         return null;
      } else if (modelPart.equals("body")) {
         return (ModelPart)Reflector.ModelTadpole_ModelRenderers.getValue(tadpolemodel, 0);
      } else {
         return modelPart.equals("tail") ? (ModelPart)Reflector.ModelTadpole_ModelRenderers.getValue(tadpolemodel, 1) : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body", "tail"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      TadpoleRenderer tadpolerenderer = new TadpoleRenderer(entityrenderdispatcher.getContext());
      tadpolerenderer.model = (TadpoleModel)modelBase;
      tadpolerenderer.shadowRadius = shadowSize;
      return tadpolerenderer;
   }
}