package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.IllusionerRenderer;
import net.minecraft.world.entity.EntityType;

public class ModelAdapterIllusioner extends ModelAdapterIllager {
   public ModelAdapterIllusioner() {
      super(EntityType.ILLUSIONER, "illusioner", 0.5F, new String[]{"illusion_illager"});
   }

   public Model makeModel() {
      IllagerModel illagermodel = new IllagerModel(bakeModelLayer(ModelLayers.ILLUSIONER));
      illagermodel.getHat().visible = true;
      return illagermodel;
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      IllusionerRenderer illusionerrenderer = new IllusionerRenderer(entityrenderdispatcher.getContext());
      illusionerrenderer.model = (IllagerModel)modelBase;
      illusionerrenderer.shadowRadius = shadowSize;
      return illusionerrenderer;
   }
}