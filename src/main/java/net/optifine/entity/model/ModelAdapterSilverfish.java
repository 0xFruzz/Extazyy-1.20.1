package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SilverfishModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.SilverfishRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.util.StrUtils;

public class ModelAdapterSilverfish extends ModelAdapter {
   public ModelAdapterSilverfish() {
      super(EntityType.SILVERFISH, "silverfish", 0.3F);
   }

   public Model makeModel() {
      return new SilverfishModel(bakeModelLayer(ModelLayers.SILVERFISH));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof SilverfishModel silverfishmodel)) {
         return null;
      } else {
         String s = "body";
         if (modelPart.startsWith(s)) {
            String s3 = StrUtils.removePrefix(modelPart, s);
            int k = Config.parseInt(s3, -1);
            int l = k - 1;
            return silverfishmodel.root().getChildModelDeep("segment" + l);
         } else {
            String s1 = "wing";
            if (modelPart.startsWith(s1)) {
               String s2 = StrUtils.removePrefix(modelPart, s1);
               int i = Config.parseInt(s2, -1);
               int j = i - 1;
               return silverfishmodel.root().getChildModelDeep("layer" + j);
            } else {
               return null;
            }
         }
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body1", "body2", "body3", "body4", "body5", "body6", "body7", "wing1", "wing2", "wing3"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      SilverfishRenderer silverfishrenderer = new SilverfishRenderer(entityrenderdispatcher.getContext());
      silverfishrenderer.model = (SilverfishModel)modelBase;
      silverfishrenderer.shadowRadius = shadowSize;
      return silverfishrenderer;
   }
}