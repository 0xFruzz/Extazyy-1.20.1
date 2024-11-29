package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundKeepAlivePacket implements Packet<ServerGamePacketListener> {
   private final long id;

   public ServerboundKeepAlivePacket(long pId) {
      this.id = pId;
   }

   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleKeepAlive(this);
   }

   public ServerboundKeepAlivePacket(FriendlyByteBuf pBuffer) {
      this.id = pBuffer.readLong();
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeLong(this.id);
   }

   public long getId() {
      return this.id;
   }
}