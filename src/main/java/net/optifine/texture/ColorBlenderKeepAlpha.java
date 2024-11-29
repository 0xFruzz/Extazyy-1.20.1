package net.optifine.texture;

public class ColorBlenderKeepAlpha implements IColorBlender {
   public int blend(int col1, int col2, int col3, int col4) {
      int i = this.alphaBlend(col1, col2);
      int j = this.alphaBlend(col3, col4);
      return this.alphaBlend(i, j);
   }

   private int alphaBlend(int c1, int c2) {
      int i = (c1 & -16777216) >> 24 & 255;
      int j = (c2 & -16777216) >> 24 & 255;
      int k = (i + j) / 2;
      if (i == 0 && j == 0) {
         i = 1;
         j = 1;
      } else {
         if (i == 0) {
            c1 = c2;
            k = j;
         }

         if (j == 0) {
            c2 = c1;
            k = i;
         }
      }

      int l = (c1 >> 16 & 255) * i;
      int i1 = (c1 >> 8 & 255) * i;
      int j1 = (c1 & 255) * i;
      int k1 = (c2 >> 16 & 255) * j;
      int l1 = (c2 >> 8 & 255) * j;
      int i2 = (c2 & 255) * j;
      int j2 = (l + k1) / (i + j);
      int k2 = (i1 + l1) / (i + j);
      int l2 = (j1 + i2) / (i + j);
      return k << 24 | j2 << 16 | k2 << 8 | l2;
   }
}