package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CodModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.CodRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterCod extends ModelAdapter {
   public ModelAdapterCod() {
      super(EntityType.COD, "cod", 0.3F);
   }

   public Model makeModel() {
      return new CodModel(bakeModelLayer(ModelLayers.COD));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof CodModel codmodel)) {
         return null;
      } else if (modelPart.equals("body")) {
         return codmodel.root().getChildModelDeep("body");
      } else if (modelPart.equals("fin_back")) {
         return codmodel.root().getChildModelDeep("top_fin");
      } else if (modelPart.equals("head")) {
         return codmodel.root().getChildModelDeep("head");
      } else if (modelPart.equals("nose")) {
         return codmodel.root().getChildModelDeep("nose");
      } else if (modelPart.equals("fin_right")) {
         return codmodel.root().getChildModelDeep("right_fin");
      } else if (modelPart.equals("fin_left")) {
         return codmodel.root().getChildModelDeep("left_fin");
      } else {
         return modelPart.equals("tail") ? codmodel.root().getChildModelDeep("tail_fin") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body", "fin_back", "head", "nose", "fin_right", "fin_left", "tail"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      CodRenderer codrenderer = new CodRenderer(entityrenderdispatcher.getContext());
      codrenderer.model = (CodModel)modelBase;
      codrenderer.shadowRadius = shadowSize;
      return codrenderer;
   }
}