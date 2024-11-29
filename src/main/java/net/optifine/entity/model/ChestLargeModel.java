package net.optifine.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ChestLargeModel extends Model {
   public ModelPart lid_left;
   public ModelPart base_left;
   public ModelPart knob_left;
   public ModelPart lid_right;
   public ModelPart base_right;
   public ModelPart knob_right;

   public ChestLargeModel() {
      super(RenderType::entityCutout);
      BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
      ChestRenderer chestrenderer = new ChestRenderer(blockentityrenderdispatcher.getContext());
      this.lid_right = (ModelPart)Reflector.TileEntityChestRenderer_modelRenderers.getValue(chestrenderer, 3);
      this.base_right = (ModelPart)Reflector.TileEntityChestRenderer_modelRenderers.getValue(chestrenderer, 4);
      this.knob_right = (ModelPart)Reflector.TileEntityChestRenderer_modelRenderers.getValue(chestrenderer, 5);
      this.lid_left = (ModelPart)Reflector.TileEntityChestRenderer_modelRenderers.getValue(chestrenderer, 6);
      this.base_left = (ModelPart)Reflector.TileEntityChestRenderer_modelRenderers.getValue(chestrenderer, 7);
      this.knob_left = (ModelPart)Reflector.TileEntityChestRenderer_modelRenderers.getValue(chestrenderer, 8);
   }

   public BlockEntityRenderer updateRenderer(BlockEntityRenderer renderer) {
      if (!Reflector.TileEntityChestRenderer_modelRenderers.exists()) {
         Config.warn("Field not found: TileEntityChestRenderer.modelRenderers");
         return null;
      } else {
         Reflector.TileEntityChestRenderer_modelRenderers.setValue(renderer, 3, this.lid_right);
         Reflector.TileEntityChestRenderer_modelRenderers.setValue(renderer, 4, this.base_right);
         Reflector.TileEntityChestRenderer_modelRenderers.setValue(renderer, 5, this.knob_right);
         Reflector.TileEntityChestRenderer_modelRenderers.setValue(renderer, 6, this.lid_left);
         Reflector.TileEntityChestRenderer_modelRenderers.setValue(renderer, 7, this.base_left);
         Reflector.TileEntityChestRenderer_modelRenderers.setValue(renderer, 8, this.knob_left);
         return renderer;
      }
   }

   public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
   }
}