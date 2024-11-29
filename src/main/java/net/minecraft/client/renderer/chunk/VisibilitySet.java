package net.minecraft.client.renderer.chunk;

import java.util.Set;
import net.minecraft.core.Direction;

public class VisibilitySet {
   private static final int FACINGS = Direction.values().length;
   private long bits;

   public void add(Set<Direction> pFaces) {
      for(Direction direction : pFaces) {
         for(Direction direction1 : pFaces) {
            this.set(direction, direction1, true);
         }
      }

   }

   public void set(Direction pFace, Direction pOtherFace, boolean pVisible) {
      this.setBit(pFace.ordinal() + pOtherFace.ordinal() * FACINGS, pVisible);
      this.setBit(pOtherFace.ordinal() + pFace.ordinal() * FACINGS, pVisible);
   }

   public void setAll(boolean pVisible) {
      if (pVisible) {
         this.bits = -1L;
      } else {
         this.bits = 0L;
      }

   }

   public boolean visibilityBetween(Direction pFace, Direction pOtherFace) {
      return this.getBit(pFace.ordinal() + pOtherFace.ordinal() * FACINGS);
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder();
      stringbuilder.append(' ');

      for(Direction direction : Direction.values()) {
         stringbuilder.append(' ').append(direction.toString().toUpperCase().charAt(0));
      }

      stringbuilder.append('\n');

      for(Direction direction2 : Direction.values()) {
         stringbuilder.append(direction2.toString().toUpperCase().charAt(0));

         for(Direction direction1 : Direction.values()) {
            if (direction2 == direction1) {
               stringbuilder.append("  ");
            } else {
               boolean flag = this.visibilityBetween(direction2, direction1);
               stringbuilder.append(' ').append((char)(flag ? 'Y' : 'n'));
            }
         }

         stringbuilder.append('\n');
      }

      return stringbuilder.toString();
   }

   private boolean getBit(int i) {
      return (this.bits & 1L << i) != 0L;
   }

   private void setBit(int i, boolean on) {
      if (on) {
         this.setBit(i);
      } else {
         this.clearBit(i);
      }

   }

   private void setBit(int i) {
      this.bits |= 1L << i;
   }

   private void clearBit(int i) {
      this.bits &= ~(1L << i);
   }
}