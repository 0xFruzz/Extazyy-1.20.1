package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;

public class ModelAdapterWitch extends ModelAdapterVillager {
   public ModelAdapterWitch() {
      super(EntityType.WITCH, "witch", 0.5F);
   }

   public Model makeModel() {
      return new WitchModel(bakeModelLayer(ModelLayers.WITCH));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof WitchModel witchmodel)) {
         return null;
      } else {
         return modelPart.equals("mole") ? witchmodel.root().getChildModelDeep("mole") : super.getModelRenderer(witchmodel, modelPart);
      }
   }

   public String[] getModelRendererNames() {
      String[] astring = super.getModelRendererNames();
      return (String[])Config.addObjectToArray(astring, "mole");
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      WitchRenderer witchrenderer = new WitchRenderer(entityrenderdispatcher.getContext());
      witchrenderer.model = (WitchModel)modelBase;
      witchrenderer.shadowRadius = shadowSize;
      return witchrenderer;
   }
}