package net.optifine.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class BellModel extends Model {
   public ModelPart bellBody;

   public BellModel() {
      super(RenderType::entityCutoutNoCull);
      BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
      BellRenderer bellrenderer = new BellRenderer(blockentityrenderdispatcher.getContext());
      this.bellBody = (ModelPart)Reflector.TileEntityBellRenderer_modelRenderer.getValue(bellrenderer);
   }

   public BlockEntityRenderer updateRenderer(BlockEntityRenderer renderer) {
      if (!Reflector.TileEntityBellRenderer_modelRenderer.exists()) {
         Config.warn("Field not found: TileEntityBellRenderer.modelRenderer");
         return null;
      } else {
         Reflector.TileEntityBellRenderer_modelRenderer.setValue(renderer, this.bellBody);
         return renderer;
      }
   }

   public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
   }
}