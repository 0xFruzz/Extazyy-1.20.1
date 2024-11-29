package com.mojang.blaze3d.vertex;

import java.util.function.Consumer;
import net.optifine.render.VertexBuilderWrapper;

public class VertexMultiConsumer {
   public static VertexConsumer create() {
      throw new IllegalArgumentException();
   }

   public static VertexConsumer create(VertexConsumer pConsumer) {
      return pConsumer;
   }

   public static VertexConsumer create(VertexConsumer pFirst, VertexConsumer pSecond) {
      return new VertexMultiConsumer.Double(pFirst, pSecond);
   }

   public static VertexConsumer create(VertexConsumer... pDelegates) {
      return new VertexMultiConsumer.Multiple(pDelegates);
   }

   static class Double extends VertexBuilderWrapper implements VertexConsumer {
      private final VertexConsumer first;
      private final VertexConsumer second;
      private boolean fixMultitextureUV;

      public Double(VertexConsumer pFirst, VertexConsumer pSecond) {
         super(pSecond);
         if (pFirst == pSecond) {
            throw new IllegalArgumentException("Duplicate delegates");
         } else {
            this.first = pFirst;
            this.second = pSecond;
            this.updateFixMultitextureUv();
         }
      }

      public VertexConsumer vertex(double pX, double pY, double pZ) {
         this.first.vertex(pX, pY, pZ);
         this.second.vertex(pX, pY, pZ);
         return this;
      }

      public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
         this.first.color(pRed, pGreen, pBlue, pAlpha);
         this.second.color(pRed, pGreen, pBlue, pAlpha);
         return this;
      }

      public VertexConsumer uv(float pU, float pV) {
         this.first.uv(pU, pV);
         this.second.uv(pU, pV);
         return this;
      }

      public VertexConsumer overlayCoords(int pU, int pV) {
         this.first.overlayCoords(pU, pV);
         this.second.overlayCoords(pU, pV);
         return this;
      }

      public VertexConsumer uv2(int pU, int pV) {
         this.first.uv2(pU, pV);
         this.second.uv2(pU, pV);
         return this;
      }

      public VertexConsumer normal(float pX, float pY, float pZ) {
         this.first.normal(pX, pY, pZ);
         this.second.normal(pX, pY, pZ);
         return this;
      }

      public void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
         if (this.fixMultitextureUV) {
            this.first.vertex(pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, pTexU / 32.0F, pTexV / 32.0F, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ);
         } else {
            this.first.vertex(pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, pTexU, pTexV, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ);
         }

         this.second.vertex(pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, pTexU, pTexV, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ);
      }

      public void endVertex() {
         this.first.endVertex();
         this.second.endVertex();
      }

      public void defaultColor(int pDefaultR, int pDefaultG, int pDefaultB, int pDefaultA) {
         this.first.defaultColor(pDefaultR, pDefaultG, pDefaultB, pDefaultA);
         this.second.defaultColor(pDefaultR, pDefaultG, pDefaultB, pDefaultA);
      }

      public void unsetDefaultColor() {
         this.first.unsetDefaultColor();
         this.second.unsetDefaultColor();
      }

      public void setRenderBlocks(boolean renderBlocks) {
         super.setRenderBlocks(renderBlocks);
         this.updateFixMultitextureUv();
      }

      private void updateFixMultitextureUv() {
         this.fixMultitextureUV = !this.first.isMultiTexture() && this.second.isMultiTexture();
      }

      public VertexConsumer getSecondaryBuilder() {
         return this.first;
      }
   }

   static class Multiple extends VertexBuilderWrapper implements VertexConsumer {
      private final VertexConsumer[] delegates;

      public Multiple(VertexConsumer[] pDelegates) {
         super(pDelegates.length > 0 ? pDelegates[0] : null);

         for(int i = 0; i < pDelegates.length; ++i) {
            for(int j = i + 1; j < pDelegates.length; ++j) {
               if (pDelegates[i] == pDelegates[j]) {
                  throw new IllegalArgumentException("Duplicate delegates");
               }
            }
         }

         this.delegates = pDelegates;
      }

      private void forEach(Consumer<VertexConsumer> pConsumer) {
         for(VertexConsumer vertexconsumer : this.delegates) {
            pConsumer.accept(vertexconsumer);
         }

      }

      public VertexConsumer vertex(double pX, double pY, double pZ) {
         this.forEach((p_167078_6_) -> {
            p_167078_6_.vertex(pX, pY, pZ);
         });
         return this;
      }

      public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
         this.forEach((p_167158_4_) -> {
            p_167158_4_.color(pRed, pGreen, pBlue, pAlpha);
         });
         return this;
      }

      public VertexConsumer uv(float pU, float pV) {
         this.forEach((p_167122_2_) -> {
            p_167122_2_.uv(pU, pV);
         });
         return this;
      }

      public VertexConsumer overlayCoords(int pU, int pV) {
         this.forEach((p_167164_2_) -> {
            p_167164_2_.overlayCoords(pU, pV);
         });
         return this;
      }

      public VertexConsumer uv2(int pU, int pV) {
         this.forEach((p_167140_2_) -> {
            p_167140_2_.uv2(pU, pV);
         });
         return this;
      }

      public VertexConsumer normal(float pX, float pY, float pZ) {
         this.forEach((p_167117_3_) -> {
            p_167117_3_.normal(pX, pY, pZ);
         });
         return this;
      }

      public void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
         this.forEach((p_167101_14_) -> {
            p_167101_14_.vertex(pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, pTexU, pTexV, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ);
         });
      }

      public void endVertex() {
         this.forEach(VertexConsumer::endVertex);
      }

      public void defaultColor(int pDefaultR, int pDefaultG, int pDefaultB, int pDefaultA) {
         this.forEach((p_167134_4_) -> {
            p_167134_4_.defaultColor(pDefaultR, pDefaultG, pDefaultB, pDefaultA);
         });
      }

      public void unsetDefaultColor() {
         this.forEach(VertexConsumer::unsetDefaultColor);
      }
   }
}