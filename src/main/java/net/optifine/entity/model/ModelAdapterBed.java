package net.optifine.entity.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.Config;

public class ModelAdapterBed extends ModelAdapter {
   public ModelAdapterBed() {
      super(BlockEntityType.BED, "bed", 0.0F);
   }

   public Model makeModel() {
      return new BedModel();
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof BedModel bedmodel)) {
         return null;
      } else if (modelPart.equals("head")) {
         return bedmodel.headPiece;
      } else if (modelPart.equals("foot")) {
         return bedmodel.footPiece;
      } else {
         ModelPart[] amodelpart = bedmodel.legs;
         if (amodelpart != null) {
            if (modelPart.equals("leg1")) {
               return amodelpart[0];
            }

            if (modelPart.equals("leg2")) {
               return amodelpart[1];
            }

            if (modelPart.equals("leg3")) {
               return amodelpart[2];
            }

            if (modelPart.equals("leg4")) {
               return amodelpart[3];
            }
         }

         return null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"head", "foot", "leg1", "leg2", "leg3", "leg4"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
      BlockEntityRenderer blockentityrenderer = rendererCache.get(BlockEntityType.BED, index, () -> {
         return new BedRenderer(blockentityrenderdispatcher.getContext());
      });
      if (!(blockentityrenderer instanceof BedRenderer)) {
         return null;
      } else if (!(modelBase instanceof BedModel)) {
         Config.warn("Not a BedModel: " + modelBase);
         return null;
      } else {
         BedModel bedmodel = (BedModel)modelBase;
         return bedmodel.updateRenderer(blockentityrenderer);
      }
   }
}