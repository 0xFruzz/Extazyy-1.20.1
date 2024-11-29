package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPingRequestPacket implements Packet<ServerStatusPacketListener> {
   private final long time;

   public ServerboundPingRequestPacket(long pTime) {
      this.time = pTime;
   }

   public ServerboundPingRequestPacket(FriendlyByteBuf pBuffer) {
      this.time = pBuffer.readLong();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeLong(this.time);
   }

   public void handle(ServerStatusPacketListener pHandler) {
      pHandler.handlePingRequest(this);
   }

   public long getTime() {
      return this.time;
   }
}