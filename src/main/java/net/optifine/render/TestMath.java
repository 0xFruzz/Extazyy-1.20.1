package net.optifine.render;

import java.util.Random;
import net.optifine.util.MathUtils;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class TestMath {
   static Random random = new Random(1L);

   public static void main(String[] args) {
      int i = 1;
      dbg("Test math: " + i);

      for(int j = 0; j < i; ++j) {
         testMatrix4f_mulTranslate();
         testMatrix4f_mulScale();
         testMatrix4f_mulQuaternion();
         testMatrix3f_mulQuaternion();
         testVector4f_transform();
         testVector3f_transform();
      }

      dbg("Done");
   }

   private static void testMatrix4f_mulTranslate() {
      Matrix4f matrix4f = new Matrix4f();
      MathUtils.setRandom(matrix4f, random);
      Matrix4f matrix4f1 = MathUtils.copy(matrix4f);
      float f = random.nextFloat();
      float f1 = random.nextFloat();
      float f2 = random.nextFloat();
      matrix4f.mul(MathUtils.makeTranslate4f(f, f1, f2));
      MathUtils.mulTranslate(matrix4f1, f, f1, f2);
      if (!matrix4f1.equals(matrix4f)) {
         dbg("*** DIFFERENT ***");
         dbg(matrix4f.toString());
         dbg(matrix4f1.toString());
      }

   }

   private static void testMatrix4f_mulScale() {
      Matrix4f matrix4f = new Matrix4f();
      MathUtils.setRandom(matrix4f, random);
      Matrix4f matrix4f1 = MathUtils.copy(matrix4f);
      float f = random.nextFloat();
      float f1 = random.nextFloat();
      float f2 = random.nextFloat();
      matrix4f.mul(MathUtils.makeScale4f(f, f1, f2));
      MathUtils.mulScale(matrix4f1, f, f1, f2);
      if (!matrix4f1.equals(matrix4f)) {
         dbg("*** DIFFERENT ***");
         dbg(matrix4f.toString());
         dbg(matrix4f1.toString());
      }

   }

   private static void testMatrix4f_mulQuaternion() {
      Matrix4f matrix4f = new Matrix4f();
      MathUtils.setRandom(matrix4f, random);
      Matrix4f matrix4f1 = MathUtils.copy(matrix4f);
      Quaternionf quaternionf = new Quaternionf(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
      matrix4f.mul(MathUtils.makeMatrix4f(quaternionf));
      MathUtils.mul(matrix4f1, quaternionf);
      if (!matrix4f1.equals(matrix4f)) {
         dbg("*** DIFFERENT ***");
         dbg(matrix4f.toString());
         dbg(matrix4f1.toString());
      }

   }

   private static void testMatrix3f_mulQuaternion() {
      Matrix3f matrix3f = new Matrix3f();
      MathUtils.setRandom(matrix3f, random);
      Matrix3f matrix3f1 = MathUtils.copy(matrix3f);
      Quaternionf quaternionf = new Quaternionf(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
      matrix3f.mul(MathUtils.makeMatrix3f(quaternionf));
      MathUtils.mul(matrix3f1, quaternionf);
      if (!matrix3f1.equals(matrix3f)) {
         dbg("*** DIFFERENT ***");
         dbg(matrix3f.toString());
         dbg(matrix3f1.toString());
      }

   }

   private static void testVector3f_transform() {
      Vector3f vector3f = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
      Vector3f vector3f1 = MathUtils.copy(vector3f);
      Matrix3f matrix3f = new Matrix3f();
      MathUtils.setRandom(matrix3f, random);
      MathUtils.transform(vector3f, matrix3f);
      float f = MathUtils.getTransformX(matrix3f, vector3f1.x(), vector3f1.y(), vector3f1.z());
      float f1 = MathUtils.getTransformY(matrix3f, vector3f1.x(), vector3f1.y(), vector3f1.z());
      float f2 = MathUtils.getTransformZ(matrix3f, vector3f1.x(), vector3f1.y(), vector3f1.z());
      vector3f1 = new Vector3f(f, f1, f2);
      if (!vector3f1.equals(vector3f)) {
         dbg("*** DIFFERENT ***");
         dbg(vector3f.toString());
         dbg(vector3f1.toString());
      }

   }

   private static void testVector4f_transform() {
      Vector4f vector4f = new Vector4f(random.nextFloat(), random.nextFloat(), random.nextFloat(), random.nextFloat());
      Vector4f vector4f1 = MathUtils.copy(vector4f);
      Matrix4f matrix4f = new Matrix4f();
      MathUtils.setRandom(matrix4f, random);
      MathUtils.transform(vector4f, matrix4f);
      float f = MathUtils.getTransformX(matrix4f, vector4f1.x(), vector4f1.y(), vector4f1.z(), vector4f1.w());
      float f1 = MathUtils.getTransformY(matrix4f, vector4f1.x(), vector4f1.y(), vector4f1.z(), vector4f1.w());
      float f2 = MathUtils.getTransformZ(matrix4f, vector4f1.x(), vector4f1.y(), vector4f1.z(), vector4f1.w());
      float f3 = MathUtils.getTransformW(matrix4f, vector4f1.x(), vector4f1.y(), vector4f1.z(), vector4f1.w());
      vector4f1 = new Vector4f(f, f1, f2, f3);
      if (!vector4f1.equals(vector4f)) {
         dbg("*** DIFFERENT ***");
         dbg(vector4f.toString());
         dbg(vector4f1.toString());
      }

   }

   private static void dbg(String str) {
      System.out.println(str);
   }
}