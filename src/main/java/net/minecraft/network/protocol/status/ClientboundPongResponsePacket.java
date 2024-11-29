package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPongResponsePacket implements Packet<ClientStatusPacketListener> {
   private final long time;

   public ClientboundPongResponsePacket(long pTime) {
      this.time = pTime;
   }

   public ClientboundPongResponsePacket(FriendlyByteBuf pBuffer) {
      this.time = pBuffer.readLong();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeLong(this.time);
   }

   public void handle(ClientStatusPacketListener pHandler) {
      pHandler.handlePongResponse(this);
   }

   public long getTime() {
      return this.time;
   }
}