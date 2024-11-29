package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterMinecart extends ModelAdapter {
   public ModelAdapterMinecart() {
      super(EntityType.MINECART, "minecart", 0.5F);
   }

   protected ModelAdapterMinecart(EntityType type, String name, float shadow) {
      super(type, name, shadow);
   }

   public Model makeModel() {
      return new MinecartModel(bakeModelLayer(ModelLayers.MINECART));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof MinecartModel minecartmodel)) {
         return null;
      } else if (modelPart.equals("bottom")) {
         return minecartmodel.root().getChildModelDeep("bottom");
      } else if (modelPart.equals("back")) {
         return minecartmodel.root().getChildModelDeep("back");
      } else if (modelPart.equals("front")) {
         return minecartmodel.root().getChildModelDeep("front");
      } else if (modelPart.equals("right")) {
         return minecartmodel.root().getChildModelDeep("right");
      } else {
         return modelPart.equals("left") ? minecartmodel.root().getChildModelDeep("left") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"bottom", "back", "front", "right", "left"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      MinecartRenderer minecartrenderer = new MinecartRenderer(entityrenderdispatcher.getContext(), ModelLayers.MINECART);
      if (!Reflector.RenderMinecart_modelMinecart.exists()) {
         Config.warn("Field not found: RenderMinecart.modelMinecart");
         return null;
      } else {
         Reflector.setFieldValue(minecartrenderer, Reflector.RenderMinecart_modelMinecart, modelBase);
         minecartrenderer.shadowRadius = shadowSize;
         return minecartrenderer;
      }
   }
}