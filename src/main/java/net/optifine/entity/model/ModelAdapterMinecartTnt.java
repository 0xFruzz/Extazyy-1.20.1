package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterMinecartTnt extends ModelAdapterMinecart {
   public ModelAdapterMinecartTnt() {
      super(EntityType.TNT_MINECART, "tnt_minecart", 0.5F);
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      TntMinecartRenderer tntminecartrenderer = new TntMinecartRenderer(entityrenderdispatcher.getContext());
      if (!Reflector.RenderMinecart_modelMinecart.exists()) {
         Config.warn("Field not found: RenderMinecart.modelMinecart");
         return null;
      } else {
         Reflector.setFieldValue(tntminecartrenderer, Reflector.RenderMinecart_modelMinecart, modelBase);
         tntminecartrenderer.shadowRadius = shadowSize;
         return tntminecartrenderer;
      }
   }
}