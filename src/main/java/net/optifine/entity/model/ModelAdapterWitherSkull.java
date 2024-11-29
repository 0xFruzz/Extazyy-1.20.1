package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterWitherSkull extends ModelAdapter {
   public ModelAdapterWitherSkull() {
      super(EntityType.WITHER_SKULL, "wither_skull", 0.0F);
   }

   public Model makeModel() {
      return new SkullModel(bakeModelLayer(ModelLayers.WITHER_SKULL));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof SkullModel skullmodel)) {
         return null;
      } else {
         return modelPart.equals("head") ? (ModelPart)Reflector.ModelSkull_head.getValue(skullmodel) : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"head"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      WitherSkullRenderer witherskullrenderer = new WitherSkullRenderer(entityrenderdispatcher.getContext());
      if (!Reflector.RenderWitherSkull_model.exists()) {
         Config.warn("Field not found: RenderWitherSkull_model");
         return null;
      } else {
         Reflector.setFieldValue(witherskullrenderer, Reflector.RenderWitherSkull_model, modelBase);
         witherskullrenderer.shadowRadius = shadowSize;
         return witherskullrenderer;
      }
   }
}