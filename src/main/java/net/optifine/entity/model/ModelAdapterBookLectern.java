package net.optifine.entity.model;

import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterBookLectern extends ModelAdapterBook {
   public ModelAdapterBookLectern() {
      super(BlockEntityType.LECTERN, "lectern_book", 0.0F);
   }

   public Model makeModel() {
      return new BookModel(bakeModelLayer(ModelLayers.BOOK));
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
      BlockEntityRenderer blockentityrenderer = rendererCache.get(BlockEntityType.LECTERN, index, () -> {
         return new LecternRenderer(blockentityrenderdispatcher.getContext());
      });
      if (!(blockentityrenderer instanceof LecternRenderer)) {
         return null;
      } else if (!Reflector.TileEntityLecternRenderer_modelBook.exists()) {
         Config.warn("Field not found: TileEntityLecternRenderer.modelBook");
         return null;
      } else {
         Reflector.setFieldValue(blockentityrenderer, Reflector.TileEntityLecternRenderer_modelBook, modelBase);
         return blockentityrenderer;
      }
   }
}