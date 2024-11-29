package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.Config;

public class ModelAdapterDecoratedPot extends ModelAdapter {
   public ModelAdapterDecoratedPot() {
      super(BlockEntityType.DECORATED_POT, "decorated_pot", 0.0F);
   }

   public Model makeModel() {
      return new DecoratedPotModel();
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof DecoratedPotModel decoratedpotmodel)) {
         return null;
      } else if (modelPart.equals("neck")) {
         return decoratedpotmodel.neck;
      } else if (modelPart.equals("front")) {
         return decoratedpotmodel.frontSide;
      } else if (modelPart.equals("back")) {
         return decoratedpotmodel.backSide;
      } else if (modelPart.equals("left")) {
         return decoratedpotmodel.leftSide;
      } else if (modelPart.equals("right")) {
         return decoratedpotmodel.rightSide;
      } else if (modelPart.equals("top")) {
         return decoratedpotmodel.top;
      } else {
         return modelPart.equals("bottom") ? decoratedpotmodel.bottom : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"neck", "front", "back", "left", "right", "top", "bottom"};
   }

   public IEntityRenderer makeEntityRender(Model model, float shadowSize, RendererCache rendererCache, int index) {
      BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
      BlockEntityRenderer blockentityrenderer = rendererCache.get(BlockEntityType.DECORATED_POT, index, () -> {
         return new DecoratedPotRenderer(blockentityrenderdispatcher.getContext());
      });
      if (!(blockentityrenderer instanceof DecoratedPotRenderer)) {
         return null;
      } else if (!(model instanceof DecoratedPotModel)) {
         Config.warn("Not a decorated pot model: " + model);
         return null;
      } else {
         DecoratedPotModel decoratedpotmodel = (DecoratedPotModel)model;
         return decoratedpotmodel.updateRenderer(blockentityrenderer);
      }
   }
}