package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterTropicalFishA extends ModelAdapter {
   public ModelAdapterTropicalFishA() {
      super(EntityType.TROPICAL_FISH, "tropical_fish_a", 0.2F);
   }

   public ModelAdapterTropicalFishA(EntityType entityType, String name, float shadowSize) {
      super(entityType, name, shadowSize);
   }

   public Model makeModel() {
      return new TropicalFishModelA(bakeModelLayer(ModelLayers.TROPICAL_FISH_SMALL));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof TropicalFishModelA tropicalfishmodela)) {
         return null;
      } else if (modelPart.equals("body")) {
         return tropicalfishmodela.root().getChildModelDeep("body");
      } else if (modelPart.equals("tail")) {
         return tropicalfishmodela.root().getChildModelDeep("tail");
      } else if (modelPart.equals("fin_right")) {
         return tropicalfishmodela.root().getChildModelDeep("right_fin");
      } else if (modelPart.equals("fin_left")) {
         return tropicalfishmodela.root().getChildModelDeep("left_fin");
      } else {
         return modelPart.equals("fin_top") ? tropicalfishmodela.root().getChildModelDeep("top_fin") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body", "tail", "fin_right", "fin_left", "fin_top"};
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
      } else if (!Reflector.RenderTropicalFish_modelA.exists()) {
         Config.warn("Model field not found: RenderTropicalFish.modelA");
         return null;
      } else {
         Reflector.RenderTropicalFish_modelA.setValue(tropicalfishrenderer1, modelBase);
         return tropicalfishrenderer1;
      }
   }
}