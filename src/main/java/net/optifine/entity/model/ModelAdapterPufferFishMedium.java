package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PufferfishMidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.PufferfishRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterPufferFishMedium extends ModelAdapter {
   public ModelAdapterPufferFishMedium() {
      super(EntityType.PUFFERFISH, "puffer_fish_medium", 0.2F);
   }

   public Model makeModel() {
      return new PufferfishMidModel(bakeModelLayer(ModelLayers.PUFFERFISH_MEDIUM));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof PufferfishMidModel pufferfishmidmodel)) {
         return null;
      } else if (modelPart.equals("body")) {
         return pufferfishmidmodel.root().getChildModelDeep("body");
      } else if (modelPart.equals("fin_right")) {
         return pufferfishmidmodel.root().getChildModelDeep("right_blue_fin");
      } else if (modelPart.equals("fin_left")) {
         return pufferfishmidmodel.root().getChildModelDeep("left_blue_fin");
      } else if (modelPart.equals("spikes_front_top")) {
         return pufferfishmidmodel.root().getChildModelDeep("top_front_fin");
      } else if (modelPart.equals("spikes_back_top")) {
         return pufferfishmidmodel.root().getChildModelDeep("top_back_fin");
      } else if (modelPart.equals("spikes_front_right")) {
         return pufferfishmidmodel.root().getChildModelDeep("right_front_fin");
      } else if (modelPart.equals("spikes_back_right")) {
         return pufferfishmidmodel.root().getChildModelDeep("right_back_fin");
      } else if (modelPart.equals("spikes_back_left")) {
         return pufferfishmidmodel.root().getChildModelDeep("left_back_fin");
      } else if (modelPart.equals("spikes_front_left")) {
         return pufferfishmidmodel.root().getChildModelDeep("left_front_fin");
      } else if (modelPart.equals("spikes_back_bottom")) {
         return pufferfishmidmodel.root().getChildModelDeep("bottom_back_fin");
      } else {
         return modelPart.equals("spikes_front_bottom") ? pufferfishmidmodel.root().getChildModelDeep("bottom_front_fin") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body", "fin_right", "fin_left", "spikes_front_top", "spikes_back_top", "spikes_front_right", "spikes_back_right", "spikes_back_left", "spikes_front_left", "spikes_back_bottom", "spikes_front_bottom"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      PufferfishRenderer pufferfishrenderer = new PufferfishRenderer(entityrenderdispatcher.getContext());
      pufferfishrenderer.shadowRadius = shadowSize;
      EntityRenderer entityrenderer = rendererCache.get(EntityType.PUFFERFISH, index, () -> {
         return pufferfishrenderer;
      });
      if (!(entityrenderer instanceof PufferfishRenderer pufferfishrenderer1)) {
         Config.warn("Not a PufferfishRenderer: " + entityrenderer);
         return null;
      } else if (!Reflector.RenderPufferfish_modelMedium.exists()) {
         Config.warn("Model field not found: RenderPufferfish.modelMedium");
         return null;
      } else {
         Reflector.RenderPufferfish_modelMedium.setValue(pufferfishrenderer1, modelBase);
         return pufferfishrenderer1;
      }
   }
}