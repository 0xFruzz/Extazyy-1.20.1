package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.util.StrUtils;

public class ModelAdapterSquid extends ModelAdapter {
   public ModelAdapterSquid() {
      super(EntityType.SQUID, "squid", 0.7F);
   }

   protected ModelAdapterSquid(EntityType entityType, String name, float shadowSize) {
      super(entityType, name, shadowSize);
   }

   public Model makeModel() {
      return new SquidModel(bakeModelLayer(ModelLayers.SQUID));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof SquidModel squidmodel)) {
         return null;
      } else if (modelPart.equals("body")) {
         return squidmodel.root().getChildModelDeep("body");
      } else {
         String s = "tentacle";
         if (modelPart.startsWith(s)) {
            String s1 = StrUtils.removePrefix(modelPart, s);
            int i = Config.parseInt(s1, -1);
            int j = i - 1;
            return squidmodel.root().getChildModelDeep("tentacle" + j);
         } else {
            return null;
         }
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body", "tentacle1", "tentacle2", "tentacle3", "tentacle4", "tentacle5", "tentacle6", "tentacle7", "tentacle8"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      SquidRenderer squidrenderer = new SquidRenderer(entityrenderdispatcher.getContext(), (SquidModel)modelBase);
      squidrenderer.model = (SquidModel)modelBase;
      squidrenderer.shadowRadius = shadowSize;
      return squidrenderer;
   }
}