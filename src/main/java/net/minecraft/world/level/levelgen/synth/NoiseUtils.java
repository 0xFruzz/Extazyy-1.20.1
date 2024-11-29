package net.minecraft.world.level.levelgen.synth;

import java.util.Locale;

public class NoiseUtils {
   public static double biasTowardsExtreme(double pValue, double pBias) {
      return pValue + Math.sin(Math.PI * pValue) * pBias / Math.PI;
   }

   public static void parityNoiseOctaveConfigString(StringBuilder pBuilder, double pXo, double pYo, double pZo, byte[] pP) {
      pBuilder.append(String.format(Locale.ROOT, "xo=%.3f, yo=%.3f, zo=%.3f, p0=%d, p255=%d", (float)pXo, (float)pYo, (float)pZo, pP[0], pP[255]));
   }

   public static void parityNoiseOctaveConfigString(StringBuilder pBuilder, double pXo, double pYo, double pZo, int[] pP) {
      pBuilder.append(String.format(Locale.ROOT, "xo=%.3f, yo=%.3f, zo=%.3f, p0=%d, p255=%d", (float)pXo, (float)pYo, (float)pZo, pP[0], pP[255]));
   }
}