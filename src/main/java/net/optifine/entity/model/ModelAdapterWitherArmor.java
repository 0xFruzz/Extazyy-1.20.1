package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.client.renderer.entity.layers.WitherArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterWitherArmor extends ModelAdapterWither {
   public ModelAdapterWitherArmor() {
      super(EntityType.WITHER, "wither_armor", 0.5F);
   }

   public Model makeModel() {
      return new WitherBossModel(bakeModelLayer(ModelLayers.WITHER_ARMOR));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      WitherBossRenderer witherbossrenderer = new WitherBossRenderer(entityrenderdispatcher.getContext());
      witherbossrenderer.model = new WitherBossModel<>(bakeModelLayer(ModelLayers.WITHER_ARMOR));
      witherbossrenderer.shadowRadius = 0.5F;
      EntityRenderer entityrenderer = rendererCache.get(EntityType.WITHER, index, () -> {
         return witherbossrenderer;
      });
      if (!(entityrenderer instanceof WitherBossRenderer witherbossrenderer1)) {
         Config.warn("Not a WitherRenderer: " + entityrenderer);
         return null;
      } else {
         WitherArmorLayer witherarmorlayer = new WitherArmorLayer(witherbossrenderer1, entityrenderdispatcher.getContext().getModelSet());
         witherarmorlayer.model = (WitherBossModel)modelBase;
         witherbossrenderer1.removeLayers(WitherArmorLayer.class);
         witherbossrenderer1.addLayer(witherarmorlayer);
         return witherbossrenderer1;
      }
   }

   public boolean setTextureLocation(IEntityRenderer er, ResourceLocation textureLocation) {
      WitherBossRenderer witherbossrenderer = (WitherBossRenderer)er;

      for(WitherArmorLayer witherarmorlayer : witherbossrenderer.getLayers(WitherArmorLayer.class)) {
         witherarmorlayer.customTextureLocation = textureLocation;
      }

      return true;
   }
}