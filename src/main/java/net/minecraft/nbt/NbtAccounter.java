package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;

public class NbtAccounter {
   public static final NbtAccounter UNLIMITED = new NbtAccounter(0L) {
      public void accountBytes(long p_128927_) {
      }
   };
   private final long quota;
   private long usage;

   public NbtAccounter(long pQuota) {
      this.quota = pQuota;
   }

   public void accountBytes(long pBytes) {
      this.usage += pBytes;
      if (this.usage > this.quota) {
         throw new RuntimeException("Tried to read NBT tag that was too big; tried to allocate: " + this.usage + "bytes where max allowed: " + this.quota);
      }
   }

   @VisibleForTesting
   public long getUsage() {
      return this.usage;
   }
}