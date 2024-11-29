package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.util.StrUtils;

public class ModelAdapterBlaze extends ModelAdapter {
   public ModelAdapterBlaze() {
      super(EntityType.BLAZE, "blaze", 0.5F);
   }

   public Model makeModel() {
      return new BlazeModel(bakeModelLayer(ModelLayers.BLAZE));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof BlazeModel blazemodel)) {
         return null;
      } else if (modelPart.equals("head")) {
         return blazemodel.root().getChildModelDeep("head");
      } else {
         String s = "stick";
         if (modelPart.startsWith(s)) {
            String s1 = StrUtils.removePrefix(modelPart, s);
            int i = Config.parseInt(s1, -1);
            int j = i - 1;
            return blazemodel.root().getChildModelDeep("part" + j);
         } else {
            return null;
         }
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"head", "stick1", "stick2", "stick3", "stick4", "stick5", "stick6", "stick7", "stick8", "stick9", "stick10", "stick11", "stick12"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      BlazeRenderer blazerenderer = new BlazeRenderer(entityrenderdispatcher.getContext());
      blazerenderer.model = (BlazeModel)modelBase;
      blazerenderer.shadowRadius = shadowSize;
      return blazerenderer;
   }
}