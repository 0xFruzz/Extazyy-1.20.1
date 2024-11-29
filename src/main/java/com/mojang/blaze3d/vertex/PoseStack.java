package com.mojang.blaze3d.vertex;

import com.google.common.collect.Queues;
import com.mojang.math.Axis;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraftforge.client.extensions.IForgePoseStack;
import net.optifine.util.MathUtils;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PoseStack implements IForgePoseStack {
   Deque<PoseStack.Pose> freeEntries = new ArrayDeque<>();
   private final Deque<PoseStack.Pose> poseStack = Util.make(Queues.newArrayDeque(), (dequeIn) -> {
      Matrix4f matrix4f = new Matrix4f();
      Matrix3f matrix3f = new Matrix3f();
      dequeIn.add(new PoseStack.Pose(matrix4f, matrix3f));
   });

   public void translate(double pX, double pY, double pZ) {
      this.translate((float)pX, (float)pY, (float)pZ);
   }

   public void translate(float pX, float pY, float pZ) {
      PoseStack.Pose posestack$pose = this.poseStack.getLast();
      posestack$pose.pose.translate(pX, pY, pZ);
   }

   public void scale(float pX, float pY, float pZ) {
      PoseStack.Pose posestack$pose = this.poseStack.getLast();
      posestack$pose.pose.scale(pX, pY, pZ);
      if (pX == pY && pY == pZ) {
         if (pX > 0.0F) {
            return;
         }

         posestack$pose.normal.scale(-1.0F);
      }

      float f = 1.0F / pX;
      float f1 = 1.0F / pY;
      float f2 = 1.0F / pZ;
      float f3 = Mth.fastInvCubeRoot(f * f1 * f2);
      posestack$pose.normal.scale(f3 * f, f3 * f1, f3 * f2);
   }

   public void mulPose(Quaternionf pQuaternion) {
      PoseStack.Pose posestack$pose = this.poseStack.getLast();
      posestack$pose.pose.rotate(pQuaternion);
      posestack$pose.normal.rotate(pQuaternion);
   }

   public void rotateAround(Quaternionf pQuaternion, float pX, float pY, float pZ) {
      PoseStack.Pose posestack$pose = this.poseStack.getLast();
      posestack$pose.pose.rotateAround(pQuaternion, pX, pY, pZ);
      posestack$pose.normal.rotate(pQuaternion);
   }

   public void pushPose() {
      PoseStack.Pose posestack$pose = this.poseStack.getLast();
      PoseStack.Pose posestack$pose1 = this.freeEntries.pollLast();
      if (posestack$pose1 != null) {
         posestack$pose1.pose.set((Matrix4fc)posestack$pose.pose);
         posestack$pose1.normal.set((Matrix3fc)posestack$pose.normal);
         this.poseStack.addLast(posestack$pose1);
      } else {
         this.poseStack.addLast(new PoseStack.Pose(new Matrix4f(posestack$pose.pose), new Matrix3f(posestack$pose.normal)));
      }

   }

   public void popPose() {
      PoseStack.Pose posestack$pose = this.poseStack.removeLast();
      if (posestack$pose != null) {
         this.freeEntries.add(posestack$pose);
      }

   }

   public PoseStack.Pose last() {
      return this.poseStack.getLast();
   }

   public boolean clear() {
      return this.poseStack.size() == 1;
   }

   public void rotateDegXp(float angle) {
      this.mulPose(Axis.XP.rotationDegrees(angle));
   }

   public void rotateDegXn(float angle) {
      this.mulPose(Axis.XN.rotationDegrees(angle));
   }

   public void rotateDegYp(float angle) {
      this.mulPose(Axis.YP.rotationDegrees(angle));
   }

   public void rotateDegYn(float angle) {
      this.mulPose(Axis.YN.rotationDegrees(angle));
   }

   public void rotateDegZp(float angle) {
      this.mulPose(Axis.ZP.rotationDegrees(angle));
   }

   public void rotateDegZn(float angle) {
      this.mulPose(Axis.ZN.rotationDegrees(angle));
   }

   public void rotateDeg(float angle, float x, float y, float z) {
      Vector3f vector3f = new Vector3f(x, y, z);
      Quaternionf quaternionf = MathUtils.rotationDegrees(vector3f, angle);
      this.mulPose(quaternionf);
   }

   public int size() {
      return this.poseStack.size();
   }

   public String toString() {
      return this.last().toString() + "Depth: " + this.poseStack.size();
   }

   public void setIdentity() {
      PoseStack.Pose posestack$pose = this.poseStack.getLast();
      posestack$pose.pose.identity();
      posestack$pose.normal.identity();
   }

   public void mulPoseMatrix(Matrix4f pMatrix) {
      (this.poseStack.getLast()).pose.mul(pMatrix);
   }

   public static final class Pose {
      final Matrix4f pose;
      final Matrix3f normal;

      Pose(Matrix4f pPose, Matrix3f pNormal) {
         this.pose = pPose;
         this.normal = pNormal;
      }

      public Matrix4f pose() {
         return this.pose;
      }

      public Matrix3f normal() {
         return this.normal;
      }

      public String toString() {
         return this.pose.toString() + this.normal.toString();
      }
   }
}