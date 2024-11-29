package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.GhastModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.util.StrUtils;

public class ModelAdapterGhast extends ModelAdapter {
   public ModelAdapterGhast() {
      super(EntityType.GHAST, "ghast", 0.5F);
   }

   public Model makeModel() {
      return new GhastModel(bakeModelLayer(ModelLayers.GHAST));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof GhastModel ghastmodel)) {
         return null;
      } else if (modelPart.equals("body")) {
         return ghastmodel.root().getChildModelDeep("body");
      } else {
         String s = "tentacle";
         if (modelPart.startsWith(s)) {
            String s1 = StrUtils.removePrefix(modelPart, s);
            int i = Config.parseInt(s1, -1);
            int j = i - 1;
            return ghastmodel.root().getChildModelDeep("tentacle" + j);
         } else {
            return null;
         }
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body", "tentacle1", "tentacle2", "tentacle3", "tentacle4", "tentacle5", "tentacle6", "tentacle7", "tentacle8", "tentacle9"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      GhastRenderer ghastrenderer = new GhastRenderer(entityrenderdispatcher.getContext());
      ghastrenderer.model = (GhastModel)modelBase;
      ghastrenderer.shadowRadius = shadowSize;
      return ghastrenderer;
   }
}