package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MagmaCubeRenderer;
import net.minecraft.world.entity.EntityType;
import net.optifine.Config;
import net.optifine.util.StrUtils;

public class ModelAdapterMagmaCube extends ModelAdapter {
   public ModelAdapterMagmaCube() {
      super(EntityType.MAGMA_CUBE, "magma_cube", 0.5F);
   }

   public Model makeModel() {
      return new LavaSlimeModel(bakeModelLayer(ModelLayers.MAGMA_CUBE));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof LavaSlimeModel lavaslimemodel)) {
         return null;
      } else if (modelPart.equals("core")) {
         return lavaslimemodel.root().getChildModelDeep("inside_cube");
      } else {
         String s = "segment";
         if (modelPart.startsWith(s)) {
            String s1 = StrUtils.removePrefix(modelPart, s);
            int i = Config.parseInt(s1, -1);
            int j = i - 1;
            return lavaslimemodel.root().getChildModelDeep("cube" + j);
         } else {
            return null;
         }
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"core", "segment1", "segment2", "segment3", "segment4", "segment5", "segment6", "segment7", "segment8"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      MagmaCubeRenderer magmacuberenderer = new MagmaCubeRenderer(entityrenderdispatcher.getContext());
      magmacuberenderer.model = (LavaSlimeModel)modelBase;
      magmacuberenderer.shadowRadius = shadowSize;
      return magmacuberenderer;
   }
}