package net.optifine;

import java.util.EnumSet;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Vec3M implements Position {
   public double x;
   public double y;
   public double z;

   public Vec3M(double xIn, double yIn, double zIn) {
      this.x = xIn;
      this.y = yIn;
      this.z = zIn;
   }

   public Vec3M(Vector3f p_i82487_1_) {
      this((double)p_i82487_1_.x(), (double)p_i82487_1_.y(), (double)p_i82487_1_.z());
   }

   public Vec3M set(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
      return this;
   }

   public Vec3M set(Vec3M vec) {
      return this.set(vec.x, vec.y, vec.z);
   }

   public Vec3M set(Vec3 vec) {
      return this.set(vec.x, vec.y, vec.z);
   }

   public Vec3M subtractReverse(Vec3M vec) {
      return this.set(vec.x - this.x, vec.y - this.y, vec.z - this.z);
   }

   public Vec3M normalize() {
      double d0 = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
      return d0 < 1.0E-4D ? this.set(0.0D, 0.0D, 0.0D) : this.set(this.x / d0, this.y / d0, this.z / d0);
   }

   public double dotProduct(Vec3M vec) {
      return this.x * vec.x + this.y * vec.y + this.z * vec.z;
   }

   public Vec3M crossProduct(Vec3M vec) {
      return this.set(this.y * vec.z - this.z * vec.y, this.z * vec.x - this.x * vec.z, this.x * vec.y - this.y * vec.x);
   }

   public Vec3M subtract(Vec3M vec) {
      return this.subtract(vec.x, vec.y, vec.z);
   }

   public Vec3M subtract(double x, double y, double z) {
      return this.add(-x, -y, -z);
   }

   public Vec3M add(Vec3M vec) {
      return this.add(vec.x, vec.y, vec.z);
   }

   public Vec3M add(double x, double y, double z) {
      return this.set(this.x + x, this.y + y, this.z + z);
   }

   public boolean isDistanceBelow(Position p_82509_1_, double p_82509_2_) {
      return this.squareDistanceTo(p_82509_1_.x(), p_82509_1_.y(), p_82509_1_.z()) < p_82509_2_ * p_82509_2_;
   }

   public double distanceTo(Vec3M vec) {
      double d0 = vec.x - this.x;
      double d1 = vec.y - this.y;
      double d2 = vec.z - this.z;
      return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
   }

   public double squareDistanceTo(Vec3M vec) {
      double d0 = vec.x - this.x;
      double d1 = vec.y - this.y;
      double d2 = vec.z - this.z;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public double squareDistanceTo(double xIn, double yIn, double zIn) {
      double d0 = xIn - this.x;
      double d1 = yIn - this.y;
      double d2 = zIn - this.z;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public Vec3M scale(double factor) {
      return this.mul(factor, factor, factor);
   }

   public Vec3M inverse() {
      return this.scale(-1.0D);
   }

   public Vec3M mul(Vec3M p_82559_1_) {
      return this.mul(p_82559_1_.x, p_82559_1_.y, p_82559_1_.z);
   }

   public Vec3M mul(double factorX, double factorY, double factorZ) {
      return this.set(this.x * factorX, this.y * factorY, this.z * factorZ);
   }

   public double length() {
      return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
   }

   public double lengthSquared() {
      return this.x * this.x + this.y * this.y + this.z * this.z;
   }

   public double horizontalDistance() {
      return Math.sqrt(this.x * this.x + this.z * this.z);
   }

   public double horizontalDistanceSqr() {
      return this.x * this.x + this.z * this.z;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof Vec3M)) {
         return false;
      } else {
         Vec3M vec3m = (Vec3M)p_equals_1_;
         if (Double.compare(vec3m.x, this.x) != 0) {
            return false;
         } else if (Double.compare(vec3m.y, this.y) != 0) {
            return false;
         } else {
            return Double.compare(vec3m.z, this.z) == 0;
         }
      }
   }

   public int hashCode() {
      long i = Double.doubleToLongBits(this.x);
      int j = (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.y);
      j = 31 * j + (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.z);
      return 31 * j + (int)(i ^ i >>> 32);
   }

   public String toString() {
      return "(" + this.x + ", " + this.y + ", " + this.z + ")";
   }

   public Vec3M lerp(Vec3M p_165921_1_, double p_165921_2_) {
      return this.set(Mth.lerp(p_165921_2_, this.x, p_165921_1_.x), Mth.lerp(p_165921_2_, this.y, p_165921_1_.y), Mth.lerp(p_165921_2_, this.z, p_165921_1_.z));
   }

   public Vec3M rotatePitch(float pitch) {
      float f = Mth.cos(pitch);
      float f1 = Mth.sin(pitch);
      double d0 = this.x;
      double d1 = this.y * (double)f + this.z * (double)f1;
      double d2 = this.z * (double)f - this.y * (double)f1;
      return this.set(d0, d1, d2);
   }

   public Vec3M rotateYaw(float yaw) {
      float f = Mth.cos(yaw);
      float f1 = Mth.sin(yaw);
      double d0 = this.x * (double)f + this.z * (double)f1;
      double d1 = this.y;
      double d2 = this.z * (double)f - this.x * (double)f1;
      return this.set(d0, d1, d2);
   }

   public Vec3M zRot(float p_82535_1_) {
      float f = Mth.cos(p_82535_1_);
      float f1 = Mth.sin(p_82535_1_);
      double d0 = this.x * (double)f + this.y * (double)f1;
      double d1 = this.y * (double)f - this.x * (double)f1;
      double d2 = this.z;
      return this.set(d0, d1, d2);
   }

   public Vec3M align(EnumSet<Direction.Axis> axes) {
      double d0 = axes.contains(Direction.Axis.X) ? (double)Mth.floor(this.x) : this.x;
      double d1 = axes.contains(Direction.Axis.Y) ? (double)Mth.floor(this.y) : this.y;
      double d2 = axes.contains(Direction.Axis.Z) ? (double)Mth.floor(this.z) : this.z;
      return this.set(d0, d1, d2);
   }

   public double getCoordinate(Direction.Axis axis) {
      return axis.choose(this.x, this.y, this.z);
   }

   public Vec3M with(Direction.Axis p_193103_1_, double p_193103_2_) {
      double d0 = p_193103_1_ == Direction.Axis.X ? p_193103_2_ : this.x;
      double d1 = p_193103_1_ == Direction.Axis.Y ? p_193103_2_ : this.y;
      double d2 = p_193103_1_ == Direction.Axis.Z ? p_193103_2_ : this.z;
      return this.set(d0, d1, d2);
   }

   public final double x() {
      return this.x;
   }

   public final double y() {
      return this.y;
   }

   public final double z() {
      return this.z;
   }

   public void setRgb(int rgb) {
      double d0 = (double)(rgb >> 16 & 255) / 255.0D;
      double d1 = (double)(rgb >> 8 & 255) / 255.0D;
      double d2 = (double)(rgb & 255) / 255.0D;
      this.set(d0, d1, d2);
   }

   public Vec3M fromRgb(int rgb) {
      this.setRgb(rgb);
      return this;
   }

   public Vec3 toVec3() {
      return new Vec3(this.x, this.y, this.z);
   }
}