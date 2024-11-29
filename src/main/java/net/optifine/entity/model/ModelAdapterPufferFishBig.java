package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PufferfishBigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.PufferfishRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterPufferFishBig extends ModelAdapter {
   public ModelAdapterPufferFishBig() {
      super(EntityType.PUFFERFISH, "puffer_fish_big", 0.2F);
   }

   public Model makeModel() {
      return new PufferfishBigModel(bakeModelLayer(ModelLayers.PUFFERFISH_BIG));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof PufferfishBigModel pufferfishbigmodel)) {
         return null;
      } else if (modelPart.equals("body")) {
         return pufferfishbigmodel.root().getChildModelDeep("body");
      } else if (modelPart.equals("fin_right")) {
         return pufferfishbigmodel.root().getChildModelDeep("right_blue_fin");
      } else if (modelPart.equals("fin_left")) {
         return pufferfishbigmodel.root().getChildModelDeep("left_blue_fin");
      } else if (modelPart.equals("spikes_front_top")) {
         return pufferfishbigmodel.root().getChildModelDeep("top_front_fin");
      } else if (modelPart.equals("spikes_middle_top")) {
         return pufferfishbigmodel.root().getChildModelDeep("top_middle_fin");
      } else if (modelPart.equals("spikes_back_top")) {
         return pufferfishbigmodel.root().getChildModelDeep("top_back_fin");
      } else if (modelPart.equals("spikes_front_right")) {
         return pufferfishbigmodel.root().getChildModelDeep("right_front_fin");
      } else if (modelPart.equals("spikes_front_left")) {
         return pufferfishbigmodel.root().getChildModelDeep("left_front_fin");
      } else if (modelPart.equals("spikes_front_bottom")) {
         return pufferfishbigmodel.root().getChildModelDeep("bottom_front_fin");
      } else if (modelPart.equals("spikes_middle_bottom")) {
         return pufferfishbigmodel.root().getChildModelDeep("bottom_middle_fin");
      } else if (modelPart.equals("spikes_back_bottom")) {
         return pufferfishbigmodel.root().getChildModelDeep("bottom_back_fin");
      } else if (modelPart.equals("spikes_back_right")) {
         return pufferfishbigmodel.root().getChildModelDeep("right_back_fin");
      } else {
         return modelPart.equals("spikes_back_left") ? pufferfishbigmodel.root().getChildModelDeep("left_back_fin") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body", "fin_right", "fin_left", "spikes_front_top", "spikes_middle_top", "spikes_back_top", "spikes_front_right", "spikes_front_left", "spikes_front_bottom", "spikes_middle_bottom", "spikes_back_bottom", "spikes_back_right", "spikes_back_left"};
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
      } else if (!Reflector.RenderPufferfish_modelBig.exists()) {
         Config.warn("Model field not found: RenderPufferfish.modelBig");
         return null;
      } else {
         Reflector.RenderPufferfish_modelBig.setValue(pufferfishrenderer1, modelBase);
         return pufferfishrenderer1;
      }
   }
}