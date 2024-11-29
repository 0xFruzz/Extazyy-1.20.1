package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ShulkerBulletRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterShulkerBullet extends ModelAdapter {
   public ModelAdapterShulkerBullet() {
      super(EntityType.SHULKER_BULLET, "shulker_bullet", 0.0F);
   }

   public Model makeModel() {
      return new ShulkerBulletModel(bakeModelLayer(ModelLayers.SHULKER_BULLET));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof ShulkerBulletModel shulkerbulletmodel)) {
         return null;
      } else {
         return modelPart.equals("bullet") ? shulkerbulletmodel.root().getChildModelDeep("main") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"bullet"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      ShulkerBulletRenderer shulkerbulletrenderer = new ShulkerBulletRenderer(entityrenderdispatcher.getContext());
      if (!Reflector.RenderShulkerBullet_model.exists()) {
         Config.warn("Field not found: RenderShulkerBullet.model");
         return null;
      } else {
         Reflector.setFieldValue(shulkerbulletrenderer, Reflector.RenderShulkerBullet_model, modelBase);
         shulkerbulletrenderer.shadowRadius = shadowSize;
         return shulkerbulletrenderer;
      }
   }
}