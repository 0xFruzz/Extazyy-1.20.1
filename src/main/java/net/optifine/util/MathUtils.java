package net.optifine.util;

import java.nio.FloatBuffer;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.joml.AxisAngle4f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public class MathUtils {
   public static final float PI = (float)Math.PI;
   public static final float PI2 = ((float)Math.PI * 2F);
   public static final float PId2 = ((float)Math.PI / 2F);
   private static final float[] ASIN_TABLE = new float[65536];

   public static float asin(float value) {
      return ASIN_TABLE[(int)((double)(value + 1.0F) * 32767.5D) & '\uffff'];
   }

   public static float acos(float value) {
      return ((float)Math.PI / 2F) - ASIN_TABLE[(int)((double)(value + 1.0F) * 32767.5D) & '\uffff'];
   }

   public static int getAverage(int[] vals) {
      if (vals.length <= 0) {
         return 0;
      } else {
         int i = getSum(vals);
         return i / vals.length;
      }
   }

   public static int getSum(int[] vals) {
      if (vals.length <= 0) {
         return 0;
      } else {
         int i = 0;

         for(int j = 0; j < vals.length; ++j) {
            int k = vals[j];
            i += k;
         }

         return i;
      }
   }

   public static int roundDownToPowerOfTwo(int val) {
      int i = Mth.smallestEncompassingPowerOfTwo(val);
      return val == i ? i : i / 2;
   }

   public static boolean equalsDelta(float f1, float f2, float delta) {
      return Math.abs(f1 - f2) <= delta;
   }

   public static float toDeg(float angle) {
      return angle * 180.0F / (float)Math.PI;
   }

   public static float toRad(float angle) {
      return angle / 180.0F * (float)Math.PI;
   }

   public static float roundToFloat(double d) {
      return (float)((double)Math.round(d * 1.0E8D) / 1.0E8D);
   }

   public static double distanceSq(BlockPos pos, double x, double y, double z) {
      return distanceSq((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), x, y, z);
   }

   public static float distanceSq(BlockPos pos, float x, float y, float z) {
      return distanceSq((float)pos.getX(), (float)pos.getY(), (float)pos.getZ(), x, y, z);
   }

   public static double distanceSq(double x1, double y1, double z1, double x2, double y2, double z2) {
      double d0 = x1 - x2;
      double d1 = y1 - y2;
      double d2 = z1 - z2;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public static float distanceSq(float x1, float y1, float z1, float x2, float y2, float z2) {
      float f = x1 - x2;
      float f1 = y1 - y2;
      float f2 = z1 - z2;
      return f * f + f1 * f1 + f2 * f2;
   }

   public static Matrix4f makeMatrixIdentity() {
      Matrix4f matrix4f = new Matrix4f();
      matrix4f.identity();
      return matrix4f;
   }

   public static float getTransformX(Matrix3f mat3, float x, float y, float z) {
      return mat3.m00 * x + mat3.m10 * y + mat3.m20 * z;
   }

   public static float getTransformY(Matrix3f mat3, float x, float y, float z) {
      return mat3.m01 * x + mat3.m11 * y + mat3.m21 * z;
   }

   public static float getTransformZ(Matrix3f mat3, float x, float y, float z) {
      return mat3.m02 * x + mat3.m12 * y + mat3.m22 * z;
   }

   public static void setRandom(Matrix3f mat3, Random r) {
      mat3.m00 = r.nextFloat();
      mat3.m10 = r.nextFloat();
      mat3.m20 = r.nextFloat();
      mat3.m01 = r.nextFloat();
      mat3.m11 = r.nextFloat();
      mat3.m21 = r.nextFloat();
      mat3.m02 = r.nextFloat();
      mat3.m12 = r.nextFloat();
      mat3.m22 = r.nextFloat();
   }

   public static void setRandom(Matrix4f mat4, Random r) {
      mat4.m00(r.nextFloat());
      mat4.m10(r.nextFloat());
      mat4.m20(r.nextFloat());
      mat4.m30(r.nextFloat());
      mat4.m01(r.nextFloat());
      mat4.m11(r.nextFloat());
      mat4.m21(r.nextFloat());
      mat4.m31(r.nextFloat());
      mat4.m02(r.nextFloat());
      mat4.m12(r.nextFloat());
      mat4.m22(r.nextFloat());
      mat4.m32(r.nextFloat());
      mat4.m03(r.nextFloat());
      mat4.m13(r.nextFloat());
      mat4.m23(r.nextFloat());
      mat4.m33(r.nextFloat());
   }

   public static float getTransformX(Matrix4f mat4, float x, float y, float z, float w) {
      return mat4.m00() * x + mat4.m10() * y + mat4.m20() * z + mat4.m30() * w;
   }

   public static float getTransformY(Matrix4f mat4, float x, float y, float z, float w) {
      return mat4.m01() * x + mat4.m11() * y + mat4.m21() * z + mat4.m31() * w;
   }

   public static float getTransformZ(Matrix4f mat4, float x, float y, float z, float w) {
      return mat4.m02() * x + mat4.m12() * y + mat4.m22() * z + mat4.m32() * w;
   }

   public static float getTransformW(Matrix4f mat4, float x, float y, float z, float w) {
      return mat4.m03() * x + mat4.m13() * y + mat4.m23() * z + mat4.m33() * w;
   }

   public static float getTransformX(Matrix4f mat4, float x, float y, float z) {
      return mat4.m00() * x + mat4.m10() * y + mat4.m20() * z + mat4.m30();
   }

   public static float getTransformY(Matrix4f mat4, float x, float y, float z) {
      return mat4.m01() * x + mat4.m11() * y + mat4.m21() * z + mat4.m31();
   }

   public static float getTransformZ(Matrix4f mat4, float x, float y, float z) {
      return mat4.m02() * x + mat4.m12() * y + mat4.m22() * z + mat4.m32();
   }

   public static void transform(Matrix4f mat4, Vector3f vec3, Vector3f dest) {
      float f = vec3.x;
      float f1 = vec3.y;
      float f2 = vec3.z;
      dest.x = mat4.m00() * f + mat4.m10() * f1 + mat4.m20() * f2 + mat4.m30();
      dest.y = mat4.m01() * f + mat4.m11() * f1 + mat4.m21() * f2 + mat4.m31();
      dest.z = mat4.m02() * f + mat4.m12() * f1 + mat4.m22() * f2 + mat4.m32();
   }

   public static boolean isIdentity(Matrix4f mat4) {
      return (mat4.properties() & 4) != 0;
   }

   public static Vector3f copy(Vector3f vec3) {
      return new Vector3f((Vector3fc)vec3);
   }

   public static Matrix4f copy(Matrix4f mat4) {
      return new Matrix4f(mat4);
   }

   public static Quaternionf rotationDegrees(Vector3f vec, float angleDeg) {
      float f = toRad(angleDeg);
      AxisAngle4f axisangle4f = new AxisAngle4f(f, vec);
      return new Quaternionf(axisangle4f);
   }

   public static Matrix3f copy(Matrix3f mat3) {
      return new Matrix3f(mat3);
   }

   public static void write(Matrix4f mat4, FloatBuffer buf) {
      buf.put(bufferIndexMat4(0, 0), mat4.m00());
      buf.put(bufferIndexMat4(0, 1), mat4.m10());
      buf.put(bufferIndexMat4(0, 2), mat4.m20());
      buf.put(bufferIndexMat4(0, 3), mat4.m30());
      buf.put(bufferIndexMat4(1, 0), mat4.m01());
      buf.put(bufferIndexMat4(1, 1), mat4.m11());
      buf.put(bufferIndexMat4(1, 2), mat4.m21());
      buf.put(bufferIndexMat4(1, 3), mat4.m31());
      buf.put(bufferIndexMat4(2, 0), mat4.m02());
      buf.put(bufferIndexMat4(2, 1), mat4.m12());
      buf.put(bufferIndexMat4(2, 2), mat4.m22());
      buf.put(bufferIndexMat4(2, 3), mat4.m32());
      buf.put(bufferIndexMat4(3, 0), mat4.m03());
      buf.put(bufferIndexMat4(3, 1), mat4.m13());
      buf.put(bufferIndexMat4(3, 2), mat4.m23());
      buf.put(bufferIndexMat4(3, 3), mat4.m33());
   }

   private static int bufferIndexMat4(int rowIn, int colIn) {
      return colIn * 4 + rowIn;
   }

   public static void write(Matrix4f mat4, float[] floatArrayIn) {
      floatArrayIn[0] = mat4.m00();
      floatArrayIn[1] = mat4.m10();
      floatArrayIn[2] = mat4.m20();
      floatArrayIn[3] = mat4.m30();
      floatArrayIn[4] = mat4.m01();
      floatArrayIn[5] = mat4.m11();
      floatArrayIn[6] = mat4.m21();
      floatArrayIn[7] = mat4.m31();
      floatArrayIn[8] = mat4.m02();
      floatArrayIn[9] = mat4.m12();
      floatArrayIn[10] = mat4.m22();
      floatArrayIn[11] = mat4.m32();
      floatArrayIn[12] = mat4.m03();
      floatArrayIn[13] = mat4.m13();
      floatArrayIn[14] = mat4.m23();
      floatArrayIn[15] = mat4.m33();
   }

   public static Vector3f makeVector3f(Vector4f vec4) {
      return new Vector3f(vec4.x, vec4.y, vec4.z);
   }

   public static void transform(Vector3f vec3, Matrix3f mat3) {
      mat3.transform(vec3);
   }

   public static void transform(Vector4f vec4, Matrix4f mat4) {
      mat4.transform(vec4);
   }

   public static void transform(Vector4f vec4, Quaternionf quat) {
      vec4.rotate(quat);
   }

   public static void store(Matrix3f mat3, FloatBuffer buf) {
      buf.put(bufferIndexMat3(0, 0), mat3.m00);
      buf.put(bufferIndexMat3(0, 1), mat3.m10);
      buf.put(bufferIndexMat3(0, 2), mat3.m20);
      buf.put(bufferIndexMat3(1, 0), mat3.m01);
      buf.put(bufferIndexMat3(1, 1), mat3.m11);
      buf.put(bufferIndexMat3(1, 2), mat3.m21);
      buf.put(bufferIndexMat3(2, 0), mat3.m02);
      buf.put(bufferIndexMat3(2, 1), mat3.m12);
      buf.put(bufferIndexMat3(2, 2), mat3.m22);
   }

   private static int bufferIndexMat3(int row, int col) {
      return col * 3 + row;
   }

   public static void mulTranslate(Matrix4f mat4, float dx, float dy, float dz) {
      mat4.translate(dx, dy, dz);
   }

   public static void mulScale(Matrix4f mat4, float x, float y, float z) {
      mat4.scale(x, y, z);
   }

   public static Matrix4f makeMatrix4f(Quaternionf quat) {
      Matrix4f matrix4f = new Matrix4f();
      matrix4f.set((Quaternionfc)quat);
      return matrix4f;
   }

   public static void mul(Matrix4f mat4, Quaternionf quat) {
      mat4.rotate(quat);
   }

   public static Matrix3f makeMatrix3f(Quaternionf quat) {
      Matrix3f matrix3f = new Matrix3f();
      matrix3f.set((Quaternionfc)quat);
      return matrix3f;
   }

   public static void mul(Matrix3f mat3, Quaternionf quat) {
      mat3.rotate(quat);
   }

   public static Matrix4f makeTranslate4f(float x, float y, float z) {
      Matrix4f matrix4f = new Matrix4f();
      matrix4f.translation(x, y, z);
      return matrix4f;
   }

   public static Matrix4f makeScale4f(float x, float y, float z) {
      Matrix4f matrix4f = new Matrix4f();
      matrix4f.scale(x, y, z);
      return matrix4f;
   }

   public static Vector4f copy(Vector4f vec4) {
      return new Vector4f((Vector4fc)vec4);
   }

   public static Matrix4f makeOrtho4f(float leftIn, float rightIn, float topIn, float bottomIn, float nearIn, float farIn) {
      return (new Matrix4f()).ortho(leftIn, rightIn, bottomIn, topIn, nearIn, farIn);
   }

   static {
      for(int i = 0; i < 65536; ++i) {
         ASIN_TABLE[i] = (float)Math.asin((double)i / 32767.5D - 1.0D);
      }

      for(int j = -1; j < 2; ++j) {
         ASIN_TABLE[(int)(((double)j + 1.0D) * 32767.5D) & '\uffff'] = (float)Math.asin((double)j);
      }

   }
}