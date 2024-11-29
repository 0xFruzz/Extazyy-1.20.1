package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.optifine.util.MathUtils;
import org.joml.Matrix4f;

public class BakedGlyph {
   private final GlyphRenderTypes renderTypes;
   private final float u0;
   private final float u1;
   private final float v0;
   private final float v1;
   private final float left;
   private final float right;
   private final float up;
   private final float down;
   public static final Matrix4f MATRIX_IDENTITY = MathUtils.makeMatrixIdentity();

   public BakedGlyph(GlyphRenderTypes pRenderTypes, float pU0, float pU1, float pV0, float pV1, float pLeft, float pRight, float pUp, float pDown) {
      this.renderTypes = pRenderTypes;
      this.u0 = pU0;
      this.u1 = pU1;
      this.v0 = pV0;
      this.v1 = pV1;
      this.left = pLeft;
      this.right = pRight;
      this.up = pUp;
      this.down = pDown;
   }

   public void render(boolean pItalic, float pX, float pY, Matrix4f pMatrix, VertexConsumer pBuffer, float pRed, float pGreen, float pBlue, float pAlpha, int pPackedLight) {
      int i = 3;
      float f = pX + this.left;
      float f1 = pX + this.right;
      float f2 = this.up - 3.0F;
      float f3 = this.down - 3.0F;
      float f4 = pY + f2;
      float f5 = pY + f3;
      float f6 = pItalic ? 1.0F - 0.25F * f2 : 0.0F;
      float f7 = pItalic ? 1.0F - 0.25F * f3 : 0.0F;
      if (pBuffer instanceof BufferBuilder bufferbuilder && ((BufferBuilder)pBuffer).canAddVertexText()) {
         int j = (int)(pRed * 255.0F);
         int k = (int)(pGreen * 255.0F);
         int l = (int)(pBlue * 255.0F);
         int i1 = (int)(pAlpha * 255.0F);
         int j1 = i1 << 24 | l << 16 | k << 8 | j;
         Matrix4f matrix4f = pMatrix == MATRIX_IDENTITY ? null : pMatrix;
         bufferbuilder.addVertexText(matrix4f, f + f6, f4, 0.0F, j1, this.u0, this.v0, pPackedLight);
         bufferbuilder.addVertexText(matrix4f, f + f7, f5, 0.0F, j1, this.u0, this.v1, pPackedLight);
         bufferbuilder.addVertexText(matrix4f, f1 + f7, f5, 0.0F, j1, this.u1, this.v1, pPackedLight);
         bufferbuilder.addVertexText(matrix4f, f1 + f6, f4, 0.0F, j1, this.u1, this.v0, pPackedLight);
      } else {
         pBuffer.vertex(pMatrix, f + f6, f4, 0.0F).color(pRed, pGreen, pBlue, pAlpha).uv(this.u0, this.v0).uv2(pPackedLight).endVertex();
         pBuffer.vertex(pMatrix, f + f7, f5, 0.0F).color(pRed, pGreen, pBlue, pAlpha).uv(this.u0, this.v1).uv2(pPackedLight).endVertex();
         pBuffer.vertex(pMatrix, f1 + f7, f5, 0.0F).color(pRed, pGreen, pBlue, pAlpha).uv(this.u1, this.v1).uv2(pPackedLight).endVertex();
         pBuffer.vertex(pMatrix, f1 + f6, f4, 0.0F).color(pRed, pGreen, pBlue, pAlpha).uv(this.u1, this.v0).uv2(pPackedLight).endVertex();
      }

   }

   public void renderEffect(BakedGlyph.Effect pEffect, Matrix4f pMatrix, VertexConsumer pBuffer, int pPackedLight) {
      if (pBuffer instanceof BufferBuilder bufferbuilder && ((BufferBuilder)pBuffer).canAddVertexText()) {
         int i = (int)(pEffect.r * 255.0F);
         int j = (int)(pEffect.g * 255.0F);
         int k = (int)(pEffect.b * 255.0F);
         int l = (int)(pEffect.a * 255.0F);
         int i1 = l << 24 | k << 16 | j << 8 | i;
         Matrix4f matrix4f = pMatrix == MATRIX_IDENTITY ? null : pMatrix;
         bufferbuilder.addVertexText(matrix4f, pEffect.x0, pEffect.y0, pEffect.depth, i1, this.u0, this.v0, pPackedLight);
         bufferbuilder.addVertexText(matrix4f, pEffect.x1, pEffect.y0, pEffect.depth, i1, this.u0, this.v1, pPackedLight);
         bufferbuilder.addVertexText(matrix4f, pEffect.x1, pEffect.y1, pEffect.depth, i1, this.u1, this.v1, pPackedLight);
         bufferbuilder.addVertexText(matrix4f, pEffect.x0, pEffect.y1, pEffect.depth, i1, this.u1, this.v0, pPackedLight);
      } else {
         pBuffer.vertex(pMatrix, pEffect.x0, pEffect.y0, pEffect.depth).color(pEffect.r, pEffect.g, pEffect.b, pEffect.a).uv(this.u0, this.v0).uv2(pPackedLight).endVertex();
         pBuffer.vertex(pMatrix, pEffect.x1, pEffect.y0, pEffect.depth).color(pEffect.r, pEffect.g, pEffect.b, pEffect.a).uv(this.u0, this.v1).uv2(pPackedLight).endVertex();
         pBuffer.vertex(pMatrix, pEffect.x1, pEffect.y1, pEffect.depth).color(pEffect.r, pEffect.g, pEffect.b, pEffect.a).uv(this.u1, this.v1).uv2(pPackedLight).endVertex();
         pBuffer.vertex(pMatrix, pEffect.x0, pEffect.y1, pEffect.depth).color(pEffect.r, pEffect.g, pEffect.b, pEffect.a).uv(this.u1, this.v0).uv2(pPackedLight).endVertex();
      }

   }

   public RenderType renderType(Font.DisplayMode pDisplayMode) {
      return this.renderTypes.select(pDisplayMode);
   }

   public static class Effect {
      protected final float x0;
      protected final float y0;
      protected final float x1;
      protected final float y1;
      protected final float depth;
      protected final float r;
      protected final float g;
      protected final float b;
      protected final float a;

      public Effect(float pX0, float pY0, float pX1, float pY1, float pDepth, float pR, float pG, float pB, float pA) {
         this.x0 = pX0;
         this.y0 = pY0;
         this.x1 = pX1;
         this.y1 = pY1;
         this.depth = pDepth;
         this.r = pR;
         this.g = pG;
         this.b = pB;
         this.a = pA;
      }
   }
}