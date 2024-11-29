package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.Config;

public class ModelAdapterTrappedChestLarge extends ModelAdapter {
   public ModelAdapterTrappedChestLarge() {
      super(BlockEntityType.TRAPPED_CHEST, "trapped_chest_large", 0.0F);
   }

   public Model makeModel() {
      return new ChestLargeModel();
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof ChestLargeModel chestlargemodel)) {
         return null;
      } else if (modelPart.equals("lid_left")) {
         return chestlargemodel.lid_left;
      } else if (modelPart.equals("base_left")) {
         return chestlargemodel.base_left;
      } else if (modelPart.equals("knob_left")) {
         return chestlargemodel.knob_left;
      } else if (modelPart.equals("lid_right")) {
         return chestlargemodel.lid_right;
      } else if (modelPart.equals("base_right")) {
         return chestlargemodel.base_right;
      } else {
         return modelPart.equals("knob_right") ? chestlargemodel.knob_right : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"lid_left", "base_left", "knob_left", "lid_right", "base_right", "knob_right"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
      BlockEntityRenderer blockentityrenderer = rendererCache.get(BlockEntityType.TRAPPED_CHEST, index, () -> {
         return new ChestRenderer(blockentityrenderdispatcher.getContext());
      });
      if (!(blockentityrenderer instanceof ChestRenderer)) {
         return null;
      } else if (!(modelBase instanceof ChestLargeModel)) {
         Config.warn("Not a large chest model: " + modelBase);
         return null;
      } else {
         ChestLargeModel chestlargemodel = (ChestLargeModel)modelBase;
         return chestlargemodel.updateRenderer(blockentityrenderer);
      }
   }
}