package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EndermiteModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EndermiteRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.util.StrUtils;

public class ModelAdapterEndermite extends ModelAdapter {
   public ModelAdapterEndermite() {
      super(EntityType.ENDERMITE, "endermite", 0.3F);
   }

   public Model makeModel() {
      return new EndermiteModel(bakeModelLayer(ModelLayers.ENDERMITE));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof EndermiteModel endermitemodel)) {
         return null;
      } else {
         String s = "body";
         if (modelPart.startsWith(s)) {
            String s1 = StrUtils.removePrefix(modelPart, s);
            int i = Config.parseInt(s1, -1);
            int j = i - 1;
            return endermitemodel.root().getChildModelDeep("segment" + j);
         } else {
            return null;
         }
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body1", "body2", "body3", "body4"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      EndermiteRenderer endermiterenderer = new EndermiteRenderer(entityrenderdispatcher.getContext());
      endermiterenderer.model = (EndermiteModel)modelBase;
      endermiterenderer.shadowRadius = shadowSize;
      return endermiterenderer;
   }
}