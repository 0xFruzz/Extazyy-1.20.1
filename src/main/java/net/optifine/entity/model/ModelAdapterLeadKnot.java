package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LeashKnotRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterLeadKnot extends ModelAdapter {
   public ModelAdapterLeadKnot() {
      super(EntityType.LEASH_KNOT, "lead_knot", 0.0F);
   }

   public Model makeModel() {
      return new LeashKnotModel(bakeModelLayer(ModelLayers.LEASH_KNOT));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof LeashKnotModel leashknotmodel)) {
         return null;
      } else {
         return modelPart.equals("knot") ? leashknotmodel.root().getChildModelDeep("knot") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"knot"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      LeashKnotRenderer leashknotrenderer = new LeashKnotRenderer(entityrenderdispatcher.getContext());
      if (!Reflector.RenderLeashKnot_leashKnotModel.exists()) {
         Config.warn("Field not found: RenderLeashKnot.leashKnotModel");
         return null;
      } else {
         Reflector.setFieldValue(leashknotrenderer, Reflector.RenderLeashKnot_leashKnotModel, modelBase);
         leashknotrenderer.shadowRadius = shadowSize;
         return leashknotrenderer;
      }
   }
}