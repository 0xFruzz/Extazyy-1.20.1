package net.optifine;

public class ComparableVersion implements Comparable<ComparableVersion> {
   private int[] elements;

   public ComparableVersion(String ver) {
      String[] astring = Config.tokenize(ver, ".");
      this.elements = new int[astring.length];

      for(int i = 0; i < astring.length; ++i) {
         String s = astring[i];
         int j = Config.parseInt(s, -1);
         this.elements[i] = j;
      }

   }

   public int compareTo(ComparableVersion cv) {
      for(int i = 0; i < this.elements.length && i < cv.elements.length; ++i) {
         if (this.elements[i] != cv.elements[i]) {
            return this.elements[i] - cv.elements[i];
         }
      }

      return this.elements.length != cv.elements.length ? this.elements.length - cv.elements.length : 0;
   }

   public int getMajor() {
      return this.elements.length < 1 ? -1 : this.elements[0];
   }

   public int getMinor() {
      return this.elements.length < 2 ? -1 : this.elements[1];
   }

   public int getPatch() {
      return this.elements.length < 3 ? -1 : this.elements[2];
   }

   public String toString() {
      return Config.arrayToString(this.elements, ".");
   }
}