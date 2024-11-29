package net.optifine.util;

import java.util.Random;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.ThreadSafeLegacyRandomSource;

public class RandomUtils {
   private static final Random random = new Random();

   public static Random getRandom() {
      return random;
   }

   public static byte[] getRandomBytes(int length) {
      byte[] abyte = new byte[length];
      random.nextBytes(abyte);
      return abyte;
   }

   public static int getRandomInt(int bound) {
      return random.nextInt(bound);
   }

   public static RandomSource makeThreadSafeRandomSource(int seed) {
      return new ThreadSafeLegacyRandomSource((long)seed);
   }
}