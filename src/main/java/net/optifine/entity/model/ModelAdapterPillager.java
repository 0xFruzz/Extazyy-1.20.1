package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.PillagerRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterPillager extends ModelAdapterIllager {
   public ModelAdapterPillager() {
      super(EntityType.PILLAGER, "pillager", 0.5F);
   }

   public Model makeModel() {
      return new IllagerModel(bakeModelLayer(ModelLayers.PILLAGER));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      PillagerRenderer pillagerrenderer = new PillagerRenderer(entityrenderdispatcher.getContext());
      pillagerrenderer.model = (IllagerModel)modelBase;
      pillagerrenderer.shadowRadius = shadowSize;
      return pillagerrenderer;
   }
}