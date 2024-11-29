package net.optifine.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ConduitModel extends Model {
   public ModelPart eye;
   public ModelPart wind;
   public ModelPart base;
   public ModelPart cage;

   public ConduitModel() {
      super(RenderType::entityCutout);
      BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
      ConduitRenderer conduitrenderer = new ConduitRenderer(blockentityrenderdispatcher.getContext());
      this.eye = (ModelPart)Reflector.TileEntityConduitRenderer_modelRenderers.getValue(conduitrenderer, 0);
      this.wind = (ModelPart)Reflector.TileEntityConduitRenderer_modelRenderers.getValue(conduitrenderer, 1);
      this.base = (ModelPart)Reflector.TileEntityConduitRenderer_modelRenderers.getValue(conduitrenderer, 2);
      this.cage = (ModelPart)Reflector.TileEntityConduitRenderer_modelRenderers.getValue(conduitrenderer, 3);
   }

   public BlockEntityRenderer updateRenderer(BlockEntityRenderer renderer) {
      if (!Reflector.TileEntityConduitRenderer_modelRenderers.exists()) {
         Config.warn("Field not found: TileEntityConduitRenderer.modelRenderers");
         return null;
      } else {
         Reflector.TileEntityConduitRenderer_modelRenderers.setValue(renderer, 0, this.eye);
         Reflector.TileEntityConduitRenderer_modelRenderers.setValue(renderer, 1, this.wind);
         Reflector.TileEntityConduitRenderer_modelRenderers.setValue(renderer, 2, this.base);
         Reflector.TileEntityConduitRenderer_modelRenderers.setValue(renderer, 3, this.cage);
         return renderer;
      }
   }

   public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
   }
}