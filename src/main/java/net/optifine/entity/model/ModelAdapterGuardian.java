package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.util.StrUtils;

public class ModelAdapterGuardian extends ModelAdapter {
   public ModelAdapterGuardian() {
      super(EntityType.GUARDIAN, "guardian", 0.5F);
   }

   public ModelAdapterGuardian(EntityType entityType, String name, float shadowSize) {
      super(entityType, name, shadowSize);
   }

   public Model makeModel() {
      return new GuardianModel(bakeModelLayer(ModelLayers.GUARDIAN));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof GuardianModel guardianmodel)) {
         return null;
      } else if (modelPart.equals("body")) {
         return guardianmodel.root().getChildModelDeep("head");
      } else if (modelPart.equals("eye")) {
         return guardianmodel.root().getChildModelDeep("eye");
      } else {
         String s = "spine";
         if (modelPart.startsWith(s)) {
            String s3 = StrUtils.removePrefix(modelPart, s);
            int k = Config.parseInt(s3, -1);
            int l = k - 1;
            return guardianmodel.root().getChildModelDeep("spike" + l);
         } else {
            String s1 = "tail";
            if (modelPart.startsWith(s1)) {
               String s2 = StrUtils.removePrefix(modelPart, s1);
               int i = Config.parseInt(s2, -1);
               int j = i - 1;
               return guardianmodel.root().getChildModelDeep("tail" + j);
            } else {
               return null;
            }
         }
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"body", "eye", "spine1", "spine2", "spine3", "spine4", "spine5", "spine6", "spine7", "spine8", "spine9", "spine10", "spine11", "spine12", "tail1", "tail2", "tail3"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      GuardianRenderer guardianrenderer = new GuardianRenderer(entityrenderdispatcher.getContext());
      guardianrenderer.model = (GuardianModel)modelBase;
      guardianrenderer.shadowRadius = shadowSize;
      return guardianrenderer;
   }
}