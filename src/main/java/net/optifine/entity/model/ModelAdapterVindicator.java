package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.VindicatorRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterVindicator extends ModelAdapterIllager {
   public ModelAdapterVindicator() {
      super(EntityType.VINDICATOR, "vindicator", 0.5F, new String[]{"vindication_illager"});
   }

   public Model makeModel() {
      return new IllagerModel(bakeModelLayer(ModelLayers.VINDICATOR));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      VindicatorRenderer vindicatorrenderer = new VindicatorRenderer(entityrenderdispatcher.getContext());
      vindicatorrenderer.model = (IllagerModel)modelBase;
      vindicatorrenderer.shadowRadius = shadowSize;
      return vindicatorrenderer;
   }
}