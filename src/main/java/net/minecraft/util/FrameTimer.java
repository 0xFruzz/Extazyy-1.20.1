package net.minecraft.util;

public class FrameTimer {
   public static final int LOGGING_LENGTH = 240;
   private final long[] loggedTimes = new long[240];
   private int logStart;
   private int logLength;
   private int logEnd;

   public void logFrameDuration(long pRunningTime) {
      this.loggedTimes[this.logEnd] = pRunningTime;
      ++this.logEnd;
      if (this.logEnd == 240) {
         this.logEnd = 0;
      }

      if (this.logLength < 240) {
         this.logStart = 0;
         ++this.logLength;
      } else {
         this.logStart = this.wrapIndex(this.logEnd + 1);
      }

   }

   public long getAverageDuration(int p_144733_) {
      int i = (this.logStart + p_144733_) % 240;
      int j = this.logStart;

      long k;
      for(k = 0L; j != i; ++j) {
         k += this.loggedTimes[j];
      }

      return k / (long)p_144733_;
   }

   public int scaleAverageDurationTo(int p_144735_, int pScale) {
      return this.scaleSampleTo(this.getAverageDuration(p_144735_), pScale, 60);
   }

   public int scaleSampleTo(long pValue, int pScale, int pDivisor) {
      double d0 = (double)pValue / (double)(1000000000L / (long)pDivisor);
      return (int)(d0 * (double)pScale);
   }

   public int getLogStart() {
      return this.logStart;
   }

   public int getLogEnd() {
      return this.logEnd;
   }

   public int wrapIndex(int pRawIndex) {
      return pRawIndex % 240;
   }

   public long[] getLog() {
      return this.loggedTimes;
   }
}