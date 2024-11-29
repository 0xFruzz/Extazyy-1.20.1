package net.optifine.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.optifine.util.MathUtils;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ModelSprite {
   private ModelPart modelRenderer = null;
   private int textureOffsetX = 0;
   private int textureOffsetY = 0;
   private float posX = 0.0F;
   private float posY = 0.0F;
   private float posZ = 0.0F;
   private int sizeX = 0;
   private int sizeY = 0;
   private int sizeZ = 0;
   private float sizeAdd = 0.0F;
   private float minU = 0.0F;
   private float minV = 0.0F;
   private float maxU = 0.0F;
   private float maxV = 0.0F;

   public ModelSprite(ModelPart modelRenderer, int textureOffsetX, int textureOffsetY, float posX, float posY, float posZ, int sizeX, int sizeY, int sizeZ, float sizeAdd) {
      this.modelRenderer = modelRenderer;
      this.textureOffsetX = textureOffsetX;
      this.textureOffsetY = textureOffsetY;
      this.posX = posX;
      this.posY = posY;
      this.posZ = posZ;
      this.sizeX = sizeX;
      this.sizeY = sizeY;
      this.sizeZ = sizeZ;
      this.sizeAdd = sizeAdd;
      this.minU = (float)textureOffsetX / modelRenderer.textureWidth;
      this.minV = (float)textureOffsetY / modelRenderer.textureHeight;
      this.maxU = (float)(textureOffsetX + sizeX) / modelRenderer.textureWidth;
      this.maxV = (float)(textureOffsetY + sizeY) / modelRenderer.textureHeight;
   }

   public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
      float f = 0.0625F;
      matrixStackIn.translate(this.posX * f, this.posY * f, this.posZ * f);
      float f1 = this.minU;
      float f2 = this.maxU;
      float f3 = this.minV;
      float f4 = this.maxV;
      if (this.modelRenderer.mirror) {
         f1 = this.maxU;
         f2 = this.minU;
      }

      if (this.modelRenderer.mirrorV) {
         f3 = this.maxV;
         f4 = this.minV;
      }

      renderItemIn2D(matrixStackIn, bufferIn, f1, f3, f2, f4, this.sizeX, this.sizeY, f * (float)this.sizeZ, this.modelRenderer.textureWidth, this.modelRenderer.textureHeight, packedLightIn, packedOverlayIn, red, green, blue, alpha);
      matrixStackIn.translate(-this.posX * f, -this.posY * f, -this.posZ * f);
   }

   public static void renderItemIn2D(PoseStack matrixStackIn, VertexConsumer bufferIn, float minU, float minV, float maxU, float maxV, int sizeX, int sizeY, float width, float texWidth, float texHeight, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
      if (width < 6.25E-4F) {
         width = 6.25E-4F;
      }

      float f = maxU - minU;
      float f1 = maxV - minV;
      float f2 = Mth.abs(f) * (texWidth / 16.0F);
      float f3 = Mth.abs(f1) * (texHeight / 16.0F);
      float f4 = 0.0F;
      float f5 = 0.0F;
      float f6 = -1.0F;
      addVertex(matrixStackIn, bufferIn, 0.0F, f3, 0.0F, red, green, blue, alpha, minU, maxV, packedOverlayIn, packedLightIn, f4, f5, f6);
      addVertex(matrixStackIn, bufferIn, f2, f3, 0.0F, red, green, blue, alpha, maxU, maxV, packedOverlayIn, packedLightIn, f4, f5, f6);
      addVertex(matrixStackIn, bufferIn, f2, 0.0F, 0.0F, red, green, blue, alpha, maxU, minV, packedOverlayIn, packedLightIn, f4, f5, f6);
      addVertex(matrixStackIn, bufferIn, 0.0F, 0.0F, 0.0F, red, green, blue, alpha, minU, minV, packedOverlayIn, packedLightIn, f4, f5, f6);
      f4 = 0.0F;
      f5 = 0.0F;
      f6 = 1.0F;
      addVertex(matrixStackIn, bufferIn, 0.0F, 0.0F, width, red, green, blue, alpha, minU, minV, packedOverlayIn, packedLightIn, f4, f5, f6);
      addVertex(matrixStackIn, bufferIn, f2, 0.0F, width, red, green, blue, alpha, maxU, minV, packedOverlayIn, packedLightIn, f4, f5, f6);
      addVertex(matrixStackIn, bufferIn, f2, f3, width, red, green, blue, alpha, maxU, maxV, packedOverlayIn, packedLightIn, f4, f5, f6);
      addVertex(matrixStackIn, bufferIn, 0.0F, f3, width, red, green, blue, alpha, minU, maxV, packedOverlayIn, packedLightIn, f4, f5, f6);
      float f7 = 0.5F * f / (float)sizeX;
      float f8 = 0.5F * f1 / (float)sizeY;
      f4 = -1.0F;
      f5 = 0.0F;
      f6 = 0.0F;

      for(int i = 0; i < sizeX; ++i) {
         float f9 = (float)i / (float)sizeX;
         float f10 = minU + f * f9 + f7;
         addVertex(matrixStackIn, bufferIn, f9 * f2, f3, width, red, green, blue, alpha, f10, maxV, packedOverlayIn, packedLightIn, f4, f5, f6);
         addVertex(matrixStackIn, bufferIn, f9 * f2, f3, 0.0F, red, green, blue, alpha, f10, maxV, packedOverlayIn, packedLightIn, f4, f5, f6);
         addVertex(matrixStackIn, bufferIn, f9 * f2, 0.0F, 0.0F, red, green, blue, alpha, f10, minV, packedOverlayIn, packedLightIn, f4, f5, f6);
         addVertex(matrixStackIn, bufferIn, f9 * f2, 0.0F, width, red, green, blue, alpha, f10, minV, packedOverlayIn, packedLightIn, f4, f5, f6);
      }

      f4 = 1.0F;
      f5 = 0.0F;
      f6 = 0.0F;

      for(int j = 0; j < sizeX; ++j) {
         float f12 = (float)j / (float)sizeX;
         float f15 = minU + f * f12 + f7;
         float f11 = f12 + 1.0F / (float)sizeX;
         addVertex(matrixStackIn, bufferIn, f11 * f2, 0.0F, width, red, green, blue, alpha, f15, minV, packedOverlayIn, packedLightIn, f4, f5, f6);
         addVertex(matrixStackIn, bufferIn, f11 * f2, 0.0F, 0.0F, red, green, blue, alpha, f15, minV, packedOverlayIn, packedLightIn, f4, f5, f6);
         addVertex(matrixStackIn, bufferIn, f11 * f2, f3, 0.0F, red, green, blue, alpha, f15, maxV, packedOverlayIn, packedLightIn, f4, f5, f6);
         addVertex(matrixStackIn, bufferIn, f11 * f2, f3, width, red, green, blue, alpha, f15, maxV, packedOverlayIn, packedLightIn, f4, f5, f6);
      }

      f4 = 0.0F;
      f5 = 1.0F;
      f6 = 0.0F;

      for(int k = 0; k < sizeY; ++k) {
         float f13 = (float)k / (float)sizeY;
         float f16 = minV + f1 * f13 + f8;
         float f18 = f13 + 1.0F / (float)sizeY;
         addVertex(matrixStackIn, bufferIn, 0.0F, f18 * f3, width, red, green, blue, alpha, minU, f16, packedOverlayIn, packedLightIn, f4, f5, f6);
         addVertex(matrixStackIn, bufferIn, f2, f18 * f3, width, red, green, blue, alpha, maxU, f16, packedOverlayIn, packedLightIn, f4, f5, f6);
         addVertex(matrixStackIn, bufferIn, f2, f18 * f3, 0.0F, red, green, blue, alpha, maxU, f16, packedOverlayIn, packedLightIn, f4, f5, f6);
         addVertex(matrixStackIn, bufferIn, 0.0F, f18 * f3, 0.0F, red, green, blue, alpha, minU, f16, packedOverlayIn, packedLightIn, f4, f5, f6);
      }

      f4 = 0.0F;
      f5 = -1.0F;
      f6 = 0.0F;

      for(int l = 0; l < sizeY; ++l) {
         float f14 = (float)l / (float)sizeY;
         float f17 = minV + f1 * f14 + f8;
         addVertex(matrixStackIn, bufferIn, f2, f14 * f3, width, red, green, blue, alpha, maxU, f17, packedOverlayIn, packedLightIn, f4, f5, f6);
         addVertex(matrixStackIn, bufferIn, 0.0F, f14 * f3, width, red, green, blue, alpha, minU, f17, packedOverlayIn, packedLightIn, f4, f5, f6);
         addVertex(matrixStackIn, bufferIn, 0.0F, f14 * f3, 0.0F, red, green, blue, alpha, minU, f17, packedOverlayIn, packedLightIn, f4, f5, f6);
         addVertex(matrixStackIn, bufferIn, f2, f14 * f3, 0.0F, red, green, blue, alpha, maxU, f17, packedOverlayIn, packedLightIn, f4, f5, f6);
      }

   }

   static void addVertex(PoseStack matrixStackIn, VertexConsumer bufferIn, float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
      PoseStack.Pose posestack$pose = matrixStackIn.last();
      Matrix4f matrix4f = posestack$pose.pose();
      Matrix3f matrix3f = posestack$pose.normal();
      float f = MathUtils.getTransformX(matrix3f, normalX, normalY, normalZ);
      float f1 = MathUtils.getTransformY(matrix3f, normalX, normalY, normalZ);
      float f2 = MathUtils.getTransformZ(matrix3f, normalX, normalY, normalZ);
      float f3 = MathUtils.getTransformX(matrix4f, x, y, z);
      float f4 = MathUtils.getTransformY(matrix4f, x, y, z);
      float f5 = MathUtils.getTransformZ(matrix4f, x, y, z);
      bufferIn.vertex(f3, f4, f5, red, green, blue, alpha, texU, texV, overlayUV, lightmapUV, f, f1, f2);
   }
}