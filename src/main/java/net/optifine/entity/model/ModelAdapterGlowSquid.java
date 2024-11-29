package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.GlowSquidRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterGlowSquid extends ModelAdapterSquid {
   public ModelAdapterGlowSquid() {
      super(EntityType.GLOW_SQUID, "glow_squid", 0.7F);
   }

   public Model makeModel() {
      return new SquidModel(bakeModelLayer(ModelLayers.GLOW_SQUID));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      GlowSquidRenderer glowsquidrenderer = new GlowSquidRenderer(entityrenderdispatcher.getContext(), (SquidModel)modelBase);
      glowsquidrenderer.model = (SquidModel)modelBase;
      glowsquidrenderer.shadowRadius = shadowSize;
      return glowsquidrenderer;
   }
}