package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.entity.CaveSpiderRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterCaveSpider extends ModelAdapterSpider {
   public ModelAdapterCaveSpider() {
      super(EntityType.CAVE_SPIDER, "cave_spider", 0.7F);
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      CaveSpiderRenderer cavespiderrenderer = new CaveSpiderRenderer(entityrenderdispatcher.getContext());
      cavespiderrenderer.model = (SpiderModel)modelBase;
      cavespiderrenderer.shadowRadius = shadowSize;
      return cavespiderrenderer;
   }
}