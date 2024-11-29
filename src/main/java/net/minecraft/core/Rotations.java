package net.minecraft.core;

import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;

public class Rotations {
   protected final float x;
   protected final float y;
   protected final float z;

   public Rotations(float pX, float pY, float pZ) {
      this.x = !Float.isInfinite(pX) && !Float.isNaN(pX) ? pX % 360.0F : 0.0F;
      this.y = !Float.isInfinite(pY) && !Float.isNaN(pY) ? pY % 360.0F : 0.0F;
      this.z = !Float.isInfinite(pZ) && !Float.isNaN(pZ) ? pZ % 360.0F : 0.0F;
   }

   public Rotations(ListTag pTag) {
      this(pTag.getFloat(0), pTag.getFloat(1), pTag.getFloat(2));
   }

   public ListTag save() {
      ListTag listtag = new ListTag();
      listtag.add(FloatTag.valueOf(this.x));
      listtag.add(FloatTag.valueOf(this.y));
      listtag.add(FloatTag.valueOf(this.z));
      return listtag;
   }

   public boolean equals(Object pOther) {
      if (!(pOther instanceof Rotations rotations)) {
         return false;
      } else {
         return this.x == rotations.x && this.y == rotations.y && this.z == rotations.z;
      }
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getZ() {
      return this.z;
   }

   public float getWrappedX() {
      return Mth.wrapDegrees(this.x);
   }

   public float getWrappedY() {
      return Mth.wrapDegrees(this.y);
   }

   public float getWrappedZ() {
      return Mth.wrapDegrees(this.z);
   }
}