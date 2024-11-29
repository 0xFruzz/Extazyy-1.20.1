package net.optifine.entity.model;

import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterBook extends ModelAdapter {
   public ModelAdapterBook() {
      super(BlockEntityType.ENCHANTING_TABLE, "enchanting_book", 0.0F, new String[]{"book"});
   }

   protected ModelAdapterBook(BlockEntityType tileEntityType, String name, float shadowSize) {
      super(tileEntityType, name, shadowSize);
   }

   public Model makeModel() {
      return new BookModel(bakeModelLayer(ModelLayers.BOOK));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof BookModel bookmodel)) {
         return null;
      } else {
         ModelPart modelpart = (ModelPart)Reflector.ModelBook_root.getValue(bookmodel);
         if (modelpart != null) {
            if (modelPart.equals("cover_right")) {
               return modelpart.getChildModelDeep("left_lid");
            }

            if (modelPart.equals("cover_left")) {
               return modelpart.getChildModelDeep("right_lid");
            }

            if (modelPart.equals("pages_right")) {
               return modelpart.getChildModelDeep("left_pages");
            }

            if (modelPart.equals("pages_left")) {
               return modelpart.getChildModelDeep("right_pages");
            }

            if (modelPart.equals("flipping_page_right")) {
               return modelpart.getChildModelDeep("flip_page1");
            }

            if (modelPart.equals("flipping_page_left")) {
               return modelpart.getChildModelDeep("flip_page2");
            }

            if (modelPart.equals("book_spine")) {
               return modelpart.getChildModelDeep("seam");
            }
         }

         return null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"cover_right", "cover_left", "pages_right", "pages_left", "flipping_page_right", "flipping_page_left", "book_spine"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
      BlockEntityRenderer blockentityrenderer = rendererCache.get(BlockEntityType.ENCHANTING_TABLE, index, () -> {
         return new EnchantTableRenderer(blockentityrenderdispatcher.getContext());
      });
      if (!(blockentityrenderer instanceof EnchantTableRenderer)) {
         return null;
      } else if (!Reflector.TileEntityEnchantmentTableRenderer_modelBook.exists()) {
         Config.warn("Field not found: TileEntityEnchantmentTableRenderer.modelBook");
         return null;
      } else {
         Reflector.setFieldValue(blockentityrenderer, Reflector.TileEntityEnchantmentTableRenderer_modelBook, modelBase);
         return blockentityrenderer;
      }
   }
}