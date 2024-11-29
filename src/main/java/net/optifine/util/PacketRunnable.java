package net.optifine.util;

import net.minecraft.network.protocol.Packet;

public class PacketRunnable implements Runnable {
   private Packet packet;
   private Runnable runnable;

   public PacketRunnable(Packet packet, Runnable runnable) {
      this.packet = packet;
      this.runnable = runnable;
   }

   public void run() {
      this.runnable.run();
   }

   public Packet getPacket() {
      return this.packet;
   }

   public String toString() {
      return "PacketRunnable: " + this.packet;
   }
}