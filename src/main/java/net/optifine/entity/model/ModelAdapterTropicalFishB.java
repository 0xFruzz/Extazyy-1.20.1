package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterTropicalFishB extends ModelAdapter {
   public ModelAdapterTropicalFishB() {
      super(EntityType.TROPICAL_FISH, "tropical_fish_b", 0.2F);
   }

   public ModelAdapterTropicalFishB(EntityType entityType, String name, float shadowSize) {
      super(entityType, name, shadowSize);
   }

   public Model makeModel() {
      return new TropicalFishModelB(bakeModelLayer(ModelLayers.TROPICAL_FISH_LARGE));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof TropicalFishModelB tropicalfishmodelb)) {
         return null;
      } else if (modelPart.equals("body")) {
         return tropicalfishmodelb.root().getChildModelDeep("body");
      } else if (modelPart.equals("tail")) {
         return tropicalfishmodelb.root().getChildModelDeep("tail");
      } else if (modelPart.equals("fin_right")) {
         return tropicalfishmodelb.root().getChildModelDeep("right_fin");
      } else if (modelPart.equals("fin_left")) {
         return tropicalfishmodelb.root().getChildModelDeep("left_fin");
      } else if (modelPart.equals("fin_top")) {
         return tropicalfishmodelb.root().getChildModelDeep("top_fin");
      } else {
         return modelPart.equals("fin_bottom") ? tropicalfishmodelb.root().getChildModelDeep("bottom_fin") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body", "tail", "fin_right", "fin_left", "fin_top", "fin_bottom"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      TropicalFishRenderer tropicalfishrenderer = new TropicalFishRenderer(entityrenderdispatcher.getContext());
      tropicalfishrenderer.shadowRadius = shadowSize;
      EntityRenderer entityrenderer = rendererCache.get(EntityType.TROPICAL_FISH, index, () -> {
         return tropicalfishrenderer;
      });
      if (!(entityrenderer instanceof TropicalFishRenderer tropicalfishrenderer1)) {
         Config.warn("Not a TropicalFishRenderer: " + entityrenderer);
         return null;
      } else if (!Reflector.RenderTropicalFish_modelB.exists()) {
         Config.warn("Model field not found: RenderTropicalFish.modelB");
         return null;
      } else {
         Reflector.RenderTropicalFish_modelB.setValue(tropicalfishrenderer1, modelBase);
         return tropicalfishrenderer1;
      }
   }
}