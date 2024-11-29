package net.minecraft.world.level.levelgen;

import java.util.function.LongFunction;
import net.minecraft.util.RandomSource;

public class WorldgenRandom extends LegacyRandomSource {
   private final RandomSource randomSource;
   private int count;

   public WorldgenRandom(RandomSource pRandomSource) {
      super(0L);
      this.randomSource = pRandomSource;
   }

   public int getCount() {
      return this.count;
   }

   public RandomSource fork() {
      return this.randomSource.fork();
   }

   public PositionalRandomFactory forkPositional() {
      return this.randomSource.forkPositional();
   }

   public int next(int pBits) {
      ++this.count;
      RandomSource randomsource = this.randomSource;
      if (randomsource instanceof LegacyRandomSource legacyrandomsource) {
         return legacyrandomsource.next(pBits);
      } else {
         return (int)(this.randomSource.nextLong() >>> 64 - pBits);
      }
   }

   public synchronized void setSeed(long pSeed) {
      if (this.randomSource != null) {
         this.randomSource.setSeed(pSeed);
      }
   }

   public long setDecorationSeed(long pLevelSeed, int pMinChunkBlockX, int pMinChunkBlockZ) {
      this.setSeed(pLevelSeed);
      long i = this.nextLong() | 1L;
      long j = this.nextLong() | 1L;
      long k = (long)pMinChunkBlockX * i + (long)pMinChunkBlockZ * j ^ pLevelSeed;
      this.setSeed(k);
      return k;
   }

   public void setFeatureSeed(long pDecorationSeed, int pIndex, int pDecorationStep) {
      long i = pDecorationSeed + (long)pIndex + (long)(10000 * pDecorationStep);
      this.setSeed(i);
   }

   public void setLargeFeatureSeed(long pBaseSeed, int pChunkX, int pChunkZ) {
      this.setSeed(pBaseSeed);
      long i = this.nextLong();
      long j = this.nextLong();
      long k = (long)pChunkX * i ^ (long)pChunkZ * j ^ pBaseSeed;
      this.setSeed(k);
   }

   public void setLargeFeatureWithSalt(long pLevelSeed, int pRegionX, int pRegionZ, int pSalt) {
      long i = (long)pRegionX * 341873128712L + (long)pRegionZ * 132897987541L + pLevelSeed + (long)pSalt;
      this.setSeed(i);
   }

   public static RandomSource seedSlimeChunk(int pChunkX, int pChunkZ, long pLevelSeed, long pSalt) {
      return RandomSource.create(pLevelSeed + (long)(pChunkX * pChunkX * 4987142) + (long)(pChunkX * 5947611) + (long)(pChunkZ * pChunkZ) * 4392871L + (long)(pChunkZ * 389711) ^ pSalt);
   }

   public static enum Algorithm {
      LEGACY(LegacyRandomSource::new),
      XOROSHIRO(XoroshiroRandomSource::new);

      private final LongFunction<RandomSource> constructor;

      private Algorithm(LongFunction<RandomSource> pConstructor) {
         this.constructor = pConstructor;
      }

      public RandomSource newInstance(long pSeed) {
         return this.constructor.apply(pSeed);
      }
   }
}