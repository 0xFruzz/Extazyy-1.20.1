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

public class ChestModel extends Model {
   public ModelPart lid;
   public ModelPart base;
   public ModelPart knob;

   public ChestModel() {
      super(RenderType::entityCutout);
      BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
      ChestRenderer chestrenderer = new ChestRenderer(blockentityrenderdispatcher.getContext());
      this.lid = (ModelPart)Reflector.TileEntityChestRenderer_modelRenderers.getValue(chestrenderer, 0);
      this.base = (ModelPart)Reflector.TileEntityChestRenderer_modelRenderers.getValue(chestrenderer, 1);
      this.knob = (ModelPart)Reflector.TileEntityChestRenderer_modelRenderers.getValue(chestrenderer, 2);
   }

   public BlockEntityRenderer updateRenderer(BlockEntityRenderer renderer) {
      if (!Reflector.TileEntityChestRenderer_modelRenderers.exists()) {
         Config.warn("Field not found: TileEntityChestRenderer.modelRenderers");
         return null;
      } else {
         Reflector.TileEntityChestRenderer_modelRenderers.setValue(renderer, 0, this.lid);
         Reflector.TileEntityChestRenderer_modelRenderers.setValue(renderer, 1, this.base);
         Reflector.TileEntityChestRenderer_modelRenderers.setValue(renderer, 2, this.knob);
         return renderer;
      }
   }

   public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
   }
}