package net.optifine.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;

public class VertexBuilderDummy implements VertexConsumer {
   private MultiBufferSource.BufferSource renderTypeBuffer = null;

   public VertexBuilderDummy(MultiBufferSource.BufferSource renderTypeBuffer) {
      this.renderTypeBuffer = renderTypeBuffer;
   }

   public MultiBufferSource.BufferSource getRenderTypeBuffer() {
      return this.renderTypeBuffer;
   }

   public VertexConsumer vertex(double x, double y, double z) {
      return this;
   }

   public VertexConsumer color(int red, int green, int blue, int alpha) {
      return this;
   }

   public VertexConsumer uv(float u, float v) {
      return this;
   }

   public VertexConsumer overlayCoords(int u, int v) {
      return this;
   }

   public VertexConsumer uv2(int u, int v) {
      return this;
   }

   public VertexConsumer normal(float x, float y, float z) {
      return this;
   }

   public void endVertex() {
   }

   public void defaultColor(int red, int green, int blue, int alpha) {
   }

   public void unsetDefaultColor() {
   }
}