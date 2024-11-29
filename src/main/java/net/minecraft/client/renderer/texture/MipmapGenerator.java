package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.optifine.Mipmaps;
import net.optifine.texture.IColorBlender;

public class MipmapGenerator {
   private static final int ALPHA_CUTOUT_CUTOFF = 96;
   private static final float[] POW22 = Util.make(new float[256], (floatsIn) -> {
      for(int i = 0; i < floatsIn.length; ++i) {
         floatsIn[i] = (float)Math.pow((double)((float)i / 255.0F), 2.2D);
      }

   });

   private MipmapGenerator() {
   }

   public static NativeImage[] generateMipLevels(NativeImage[] pImages, int pMipLevel) {
      return generateMipLevels(pImages, pMipLevel, (IColorBlender)null);
   }

   public static NativeImage[] generateMipLevels(NativeImage[] imageIn, int mipmapLevelsIn, IColorBlender colorBlender) {
      if (mipmapLevelsIn + 1 <= imageIn.length) {
         return imageIn;
      } else {
         NativeImage[] anativeimage = new NativeImage[mipmapLevelsIn + 1];
         anativeimage[0] = imageIn[0];
         boolean flag = false;

         for(int i = 1; i <= mipmapLevelsIn; ++i) {
            if (i < imageIn.length) {
               anativeimage[i] = imageIn[i];
            } else {
               NativeImage nativeimage = anativeimage[i - 1];
               int j = Math.max(nativeimage.getWidth() >> 1, 1);
               int k = Math.max(nativeimage.getHeight() >> 1, 1);
               NativeImage nativeimage1 = new NativeImage(j, k, false);
               int l = nativeimage1.getWidth();
               int i1 = nativeimage1.getHeight();

               for(int j1 = 0; j1 < l; ++j1) {
                  for(int k1 = 0; k1 < i1; ++k1) {
                     if (colorBlender != null) {
                        nativeimage1.setPixelRGBA(j1, k1, colorBlender.blend(nativeimage.getPixelRGBA(j1 * 2 + 0, k1 * 2 + 0), nativeimage.getPixelRGBA(j1 * 2 + 1, k1 * 2 + 0), nativeimage.getPixelRGBA(j1 * 2 + 0, k1 * 2 + 1), nativeimage.getPixelRGBA(j1 * 2 + 1, k1 * 2 + 1)));
                     } else {
                        nativeimage1.setPixelRGBA(j1, k1, alphaBlend(nativeimage.getPixelRGBA(j1 * 2 + 0, k1 * 2 + 0), nativeimage.getPixelRGBA(j1 * 2 + 1, k1 * 2 + 0), nativeimage.getPixelRGBA(j1 * 2 + 0, k1 * 2 + 1), nativeimage.getPixelRGBA(j1 * 2 + 1, k1 * 2 + 1), flag));
                     }
                  }
               }

               anativeimage[i] = nativeimage1;
            }
         }

         return anativeimage;
      }
   }

   private static boolean hasTransparentPixel(NativeImage pImage) {
      for(int i = 0; i < pImage.getWidth(); ++i) {
         for(int j = 0; j < pImage.getHeight(); ++j) {
            if (pImage.getPixelRGBA(i, j) >> 24 == 0) {
               return true;
            }
         }
      }

      return false;
   }

   private static int alphaBlend(int pCol0, int pCol1, int pCol2, int pCol3, boolean pTransparent) {
      return Mipmaps.alphaBlend(pCol0, pCol1, pCol2, pCol3);
   }

   private static int gammaBlend(int pCol0, int pCol1, int pCol2, int pCol3, int pBitOffset) {
      float f = getPow22(pCol0 >> pBitOffset);
      float f1 = getPow22(pCol1 >> pBitOffset);
      float f2 = getPow22(pCol2 >> pBitOffset);
      float f3 = getPow22(pCol3 >> pBitOffset);
      float f4 = (float)((double)((float)Math.pow((double)(f + f1 + f2 + f3) * 0.25D, 0.45454545454545453D)));
      return (int)((double)f4 * 255.0D);
   }

   private static float getPow22(int pValue) {
      return POW22[pValue & 255];
   }
}