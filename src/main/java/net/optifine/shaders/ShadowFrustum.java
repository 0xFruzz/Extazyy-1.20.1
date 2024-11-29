package net.optifine.shaders;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.optifine.Config;
import net.optifine.util.MathUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ShadowFrustum extends Frustum {
   public ShadowFrustum(Matrix4f matrixIn, Matrix4f projectionIn) {
      super(matrixIn, projectionIn);
      this.extendForShadows(matrixIn, projectionIn);
      super.usePlanes = true;
   }

   private void extendForShadows(Matrix4f matrixIn, Matrix4f projectionIn) {
      ClientLevel clientlevel = Config.getMinecraft().level;
      if (clientlevel != null) {
         Matrix4f matrix4f = MathUtils.copy(projectionIn);
         matrix4f.mul(matrixIn);
         matrix4f.transpose();
         Vector4f vector4f = new Vector4f(0.0F, 1.0F, 0.0F, 0.0F);
         MathUtils.transform(vector4f, matrix4f);
         vector4f.normalize();
         Vector4f vector4f1 = new Vector4f(-1.0F, 0.0F, 0.0F, 0.0F);
         MathUtils.transform(vector4f1, matrix4f);
         vector4f1.normalize();
         float f = 0.0F;
         float f1 = clientlevel.getSunAngle(f);
         float f2 = Shaders.sunPathRotation * Mth.deg2Rad;
         float f3 = f1 > Mth.PId2 && f1 < 3.0F * Mth.PId2 ? f1 + (float)Math.PI : f1;
         float f4 = -Mth.sin(f3);
         float f5 = Mth.cos(f3) * Mth.cos(f2);
         float f6 = -Mth.cos(f3) * Mth.sin(f2);
         Vector4f vector4f2 = new Vector4f(f4, f5, f6, 0.0F);
         vector4f2.normalize();
         Vector3f vector3f = MathUtils.makeVector3f(vector4f);
         vector3f.mul(vector4f.dot(vector4f2));
         Vector3f vector3f1 = MathUtils.makeVector3f(vector4f2);
         vector3f1.sub(vector3f);
         vector3f1.normalize();
         Vector4f vector4f3 = new Vector4f(vector3f1.x(), vector3f1.y(), vector3f1.z(), 0.0F);
         Vector3f vector3f2 = MathUtils.makeVector3f(vector4f1);
         vector3f2.mul(vector4f1.dot(vector4f2));
         Vector3f vector3f3 = MathUtils.makeVector3f(vector4f2);
         vector3f3.sub(vector3f2);
         vector3f3.normalize();
         Vector4f vector4f4 = new Vector4f(vector3f3.x(), vector3f3.y(), vector3f3.z(), 0.0F);
         Vector4f vector4f5 = this.frustum[0];
         Vector4f vector4f6 = this.frustum[1];
         Vector4f vector4f7 = this.frustum[2];
         Vector4f vector4f8 = this.frustum[3];
         Vector4f vector4f9 = this.frustum[4];
         Vector4f vector4f10 = this.frustum[5];
         vector4f5.normalize();
         vector4f6.normalize();
         vector4f7.normalize();
         vector4f8.normalize();
         vector4f9.normalize();
         vector4f10.normalize();
         float f7 = vector4f5.dot(vector4f3);
         float f8 = vector4f6.dot(vector4f3);
         float f9 = vector4f7.dot(vector4f4);
         float f10 = vector4f8.dot(vector4f4);
         float f11 = Config.getGameRenderer().getRenderDistance();
         float f12 = Config.isFogOff() ? 1.414F : 1.0F;
         float f13 = 0.0F;
         float f14 = 0.0F;
         if (f7 < 0.0F || f8 < 0.0F) {
            vector4f10.add(0.0F, 0.0F, 0.0F, f11);
            if (f7 < 0.0F && f8 < 0.0F) {
               f13 = this.rotateDotPlus(vector4f5, vector4f3, -1, vector4f);
               f14 = this.rotateDotPlus(vector4f6, vector4f3, 1, vector4f);
               vector4f5.set(-vector4f5.x(), -vector4f5.y(), -vector4f5.z(), -vector4f5.w());
               vector4f6.set(-vector4f6.x(), -vector4f6.y(), -vector4f6.z(), -vector4f6.w());
               float f15 = -f7 * f11 * f12;
               float f16 = -f8 * f11 * f12;
               vector4f5.add(0.0F, 0.0F, 0.0F, f15);
               vector4f6.add(0.0F, 0.0F, 0.0F, f16);
            } else if (f7 < 0.0F) {
               f13 = this.rotateDotPlus(vector4f5, vector4f3, -1, vector4f);
            } else {
               f14 = this.rotateDotPlus(vector4f6, vector4f3, 1, vector4f);
            }
         }

         int i = clientlevel.getMinBuildHeight();
         int j = clientlevel.getMaxBuildHeight();
         float f17 = (float)((int)Config.getMinecraft().player.getEyeY());
         float f18 = Config.limit(f17 - (float)i, 0.0F, f11);
         float f19 = Config.limit((float)j - f17, 0.0F, f11);
         float f20 = 0.0F;
         float f21 = 0.0F;
         if (f9 < 0.0F || f10 < 0.0F) {
            vector4f10.add(0.0F, 0.0F, 0.0F, f11);
            if (f9 < 0.0F && f10 < 0.0F) {
               f20 = this.rotateDotPlus(vector4f7, vector4f4, -1, vector4f1);
               f21 = this.rotateDotPlus(vector4f8, vector4f4, 1, vector4f1);
               vector4f7.set(-vector4f7.x(), -vector4f7.y(), -vector4f7.z(), -vector4f7.w());
               vector4f8.set(-vector4f8.x(), -vector4f8.y(), -vector4f8.z(), -vector4f8.w());
               float f22 = -f9 * f19;
               float f23 = -f10 * f18;
               vector4f7.add(0.0F, 0.0F, 0.0F, f22);
               vector4f8.add(0.0F, 0.0F, 0.0F, f23);
            } else if (f9 < 0.0F) {
               f20 = this.rotateDotPlus(vector4f7, vector4f4, -1, vector4f1);
            } else {
               f21 = this.rotateDotPlus(vector4f8, vector4f4, 1, vector4f1);
            }
         }

      }
   }

   private float rotateDotPlus(Vector4f vecFrustum, Vector4f vecSun, int angleDeg, Vector4f vecRot) {
      Vector3f vector3f = MathUtils.makeVector3f(vecRot);
      Quaternionf quaternionf = MathUtils.rotationDegrees(vector3f, (float)angleDeg);
      float f = 0.0F;

      while(true) {
         float f1 = vecFrustum.dot(vecSun);
         if (f1 >= 0.0F) {
            return f;
         }

         MathUtils.transform(vecFrustum, quaternionf);
         vecFrustum.normalize();
         f += (float)angleDeg;
      }
   }
}